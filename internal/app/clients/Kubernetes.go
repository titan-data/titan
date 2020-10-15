package clients

import (
	"context"
	"errors"
	"fmt"
	titanclient "github.com/titan-data/titan-client-go"
	v1Apps "k8s.io/api/apps/v1"
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	k8s "k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
	"path/filepath"
	"strconv"
	"strings"
	"time"
)

type kubernetes struct {
	namespace string
}

func Kubernetes (n string) kubernetes {
	return kubernetes{n}
}

var client *k8s.Clientset
var ctx = context.Background()

func init() {
	home := homedir.HomeDir()
	kubeconfig := filepath.Join(home, ".kube", "config")
	config, err := clientcmd.BuildConfigFromFlags("", kubeconfig)
	if err == nil {
		client, _ = k8s.NewForConfig(config)
	}
}

/**
 * For our repositories, we keep it very simple. There is a single headless service that is responsible for exposing
 * the ports in the container. We then create a single replica stateful set with the given volumes (each with
 * existing PVCs) mapped in.
 */
func (k kubernetes) CreateStatefulSet(repoName string, imageId string, ports []int, volumes []titanclient.Volume, environment []string) {
	objectMeta := metav1.ObjectMeta{
		Name:                       repoName,
		Namespace:                  k.namespace,
		Labels:                     map[string]string{"titanRepository": repoName},
	}
	servicePorts := make([]v1.ServicePort, len(ports))
	for _, port := range ports {
		servicePorts = append(servicePorts, v1.ServicePort{
			Name:        "port-" + strconv.Itoa(port),
			Port:        int32(port),
		})
	}
	serviceSpec := v1.ServiceSpec{
		Ports:                    servicePorts,
		Selector:                 map[string]string{"titanRepository": repoName},
		ClusterIP:                "None",
	}
	service := v1.Service{
		ObjectMeta: objectMeta,
		Spec:      	serviceSpec,
		Status:     v1.ServiceStatus{},
	}
	createMetadata := metav1.CreateOptions{
		DryRun:       nil,
		FieldManager: "",
	}
	client.CoreV1().Services(k.namespace).Create(ctx, &service, createMetadata)

	containerPorts := make([]v1.ContainerPort, len(ports))
	for _, port := range ports {
		containerPorts = append(containerPorts, v1.ContainerPort{
			Name:          "port-" + strconv.Itoa(port),
			ContainerPort: int32(port),
		})
	}
	envs := make([]v1.EnvVar, len(environment))
	for _, environment := range environment {
		s := strings.Split(environment, "=")
		envs = append(envs, v1.EnvVar{
			Name:      s[0],
			Value:     s[1],
		})
	}
	volumeMounts := make([]v1.VolumeMount, len(volumes))
	for _, volume := range volumes {
		volumeMounts = append(volumeMounts, v1.VolumeMount{
			Name:             volume.Name,
			MountPath:        volume.Properties["path"].(string),
		})
	}
	container := v1.Container{
		Name:                     repoName,
		Image:                    imageId,
		Ports:                    containerPorts,
		Env:                      envs,
		VolumeMounts:             volumeMounts,
	}
	containers := make([]v1.Container, 1)
	containers = append(containers, container)

	vols := make([]v1.Volume, len(volumes))
	for _, volume := range volumes {
		pvc := v1.PersistentVolumeClaimVolumeSource{
			ClaimName: volume.Properties["pvc"].(string),
		}
		vols = append(vols, v1.Volume{
			Name:         volume.Name,
			VolumeSource: v1.VolumeSource{
				PersistentVolumeClaim: &pvc,
			},
		})
	}
	podSpec := v1.PodSpec{
		Volumes:                       vols,
		Containers:                    containers,
	}
	podTemplate := v1.PodTemplateSpec{
		ObjectMeta: objectMeta,
		Spec:       podSpec,
	}
	replica := int32(1)
	selector := metav1.LabelSelector{
		MatchLabels:      map[string]string{"titanRepository": repoName},
	}
	statefulSpecs := v1Apps.StatefulSetSpec{
		Replicas:             &replica,
		Selector:             &selector,
		Template:             podTemplate,
		ServiceName:          repoName,
	}
	statefulSet := v1Apps.StatefulSet{
		ObjectMeta: objectMeta,
		Spec:       statefulSpecs,
	}
	client.AppsV1().StatefulSets(k.namespace).Create(ctx, &statefulSet, createMetadata)
}

/**
 * Gets the status of a stateful set. We use the following:
 *
 *      detached        No such statefulset present (user deleted it)
 *      updating        Update revision doesn't match current revision
 *      stopped         Number of replicas is 0
 *      running         Number of replicas and ready replicas is 1
 *      failed          Terminal condition prevented stateful set from starting
 *      starting        Number of replicas is 1 but ready replicas is 0
 *
 * We also return a pair, with the second element providing additional context for the "failed" state
 */
func (k kubernetes) GetStatefulSetStatus(repoName string) (string, error) {
	set, err := client.AppsV1().StatefulSets(k.namespace).Get(ctx, repoName, metav1.GetOptions{})
	if err != nil {
		//TODO catch detached
		//} catch (e: ApiException) {
		//	if (e.code == 404) {
		//		return "detached" to null
		//	} else {
		//		throw e
		//	}
		//}
	}
	if set == nil {
		return "unknown", nil
	}
	if set.Status.UpdateRevision != set.Status.CurrentRevision {
		return "update", nil
	}
	if set.Status.Replicas == 0 {
		return "stopped", nil
	}
	if set.Status.Replicas == set.Status.ReadyReplicas {
		return "running", nil
	}
	pod, err := client.CoreV1().Pods(k.namespace).Get(ctx, repoName, metav1.GetOptions{})
	if err != nil {
		//TODO check for non-existing pod
	}
	conditions := pod.Status.Conditions
	if conditions != nil {
		for _, condition := range conditions {
			if condition.Reason == "Unschedulable" {
				return "failed", errors.New("Pod failed to be scheduled: " + condition.Message)
			}
		}
	}
	return "starting", nil
}

/**
 * Wait for the given statefulset to reach a terminal state (running or stopped), throwing an error if we've
 * reached the failed state.
 */
func (k kubernetes) WaitForStatefulSet(repoName string) {
	check := true
	for check {
		status, err := k.GetStatefulSetStatus(repoName)
		if status == "failed" {
			panic(err)
		}
		if status == "running" || status == "stopped" {
			check = false
		}
		time.Sleep(1000)
	}
}

/**
 * Forward port for a container. For now, we're using a temporary solution of launching 'kubectl-forward' in the
 * background. This is totally brittle, as the commands will fail in the background as pods are stopped and
 * connections broken. And if you restart the host system, there is no way to restart them. But it's a quick
 * hack to demonstrate the desired experience until we can build out a more full-featured port forwarder, such
 * as: https://github.com/pixel-point/kube-forwarder
 */
func (k kubernetes) StartPortForwarding(repoName string) {
	// There can be a race condition where even though the pod is listed as ready port forwarding fails
	time.Sleep(500)
	service, _ := client.CoreV1().Services(k.namespace).Get(ctx, repoName, metav1.GetOptions{})
	ports := service.Spec.Ports
	if ports != nil {
		for _, port := range ports {
			ce.Exec("sh", "-c", "kubectl port-forward svc/" + repoName + " " +  fmt.Sprint(port.Port) + " > /dev/null 2>&1 &")
		}
	}
}

/**
 * This is horribly OS-specific, and should be replaced with a more complete solution as described above.
 */
func (k kubernetes)StopPortForwarding(repoName string) {
	service, _ := client.CoreV1().Services(k.namespace).Get(ctx, repoName, metav1.GetOptions{})
	ports := service.Spec.Ports
	if len(ports) > 0 {
		for _, port := range ports {
			out, _ := ce.Exec("sh", "-c", "ps -ef | grep \\\"[k]ubectl port-forward svc/"+repoName+" "+fmt.Sprint(port.Port)+"\\\"")
			pid := strings.Split(out, " ")
			ce.Exec("kill", pid[2])
		}
	}
}

/**
 * Update the volumes within a given StatefulSet.
 */
func (k kubernetes)UpdateStatefulSetVolumes(repoName string, volumes []titanclient.Volume) {
	set, _ := client.AppsV1().StatefulSets(k.namespace).Get(ctx, repoName, metav1.GetOptions{})
	var p string
	specVolumes := set.Spec.Template.Spec.Volumes
	if len(specVolumes) > 0 {
		for volumeIdx, volumeDef := range specVolumes{
			for _, vol := range volumes {
				if vol.Name == volumeDef.Name {
					p = p + "{\\\"op\\\":\\\"replace\\\",\\\"path\\\":\\\"/spec/template/spec/volumes/" + strconv.Itoa(volumeIdx) + "/persistentVolumeClaim/claimName\\\",\\\"value\\\":\\\"" + vol.Config["pvc"].(string) + "\\\"}"
				}
			}
		}
	}
	client.AppsV1().StatefulSets(k.namespace).Patch(ctx, repoName, types.JSONPatchType, []byte(p), metav1.PatchOptions{})
}

func (k kubernetes)DeleteStatefulSpec(repoName string) {
	err := client.AppsV1().StatefulSets(k.namespace).Delete(ctx, repoName, metav1.DeleteOptions{})
	if err != nil {
		panic(err)
	}
	err = client.CoreV1().Services(k.namespace).Delete(ctx, repoName, metav1.DeleteOptions{})
	if err != nil {
		panic(err)
	}
}

/**
 * Stops a stateful set. This is equivalent to setting the number of replicas to zero and waiting for the
 * deployment to update. Its up to callers to wait for the changes to take effect.
 */
func (k kubernetes)StopStatefulSet(repoName string) {
	patch := []byte("[{\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":0}]")
	client.AppsV1().StatefulSets(k.namespace).Patch(ctx, repoName, types.JSONPatchType, patch, metav1.PatchOptions{})
}

/**
 * Opposite of the above, set the number of replicas to one.
 */
func (k kubernetes)StartStatefulSet(repoName string) {
	patch := []byte("[{\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":1}]")
	client.AppsV1().StatefulSets(k.namespace).Patch(ctx, repoName, types.JSONPatchType, patch, metav1.PatchOptions{})
}