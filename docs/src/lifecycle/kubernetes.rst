.. _lifecycle_kubernetes:

Titan with Kubernetes
=====================

Titan provides a way to run repositories in different container environments,
known as "contexts" (see :ref:`lifecycle_context` for more information). A
Kubernetes context represents a set of repositories running in a cluster,
accessed via the Kubernetes API. This cluster could be local to the machine,
hosted centrally, or delivered as a cloud service. Through Titan, not only can
these repositories be run in a simple fashion with powerful data controls, data
can be shared between them (such as pushing a dataset from a CI/CD Kubernetes
cluster and later cloning for local debugging).

Kubernetes Requirements
-----------------------

Titan requires a Kubernetes cluster with the following configuration options:

* The there must be a CSI (Container Storage Interface) driver installed that
  supports the `alpha snapshot <https://kubernetes-csi.github.io/docs/snapshot-restore-feature.html>`
  capabilities. Titan does not yet work with the
  `beta snapshots apis <https://kubernetes.io/blog/2019/12/09/kubernetes-1-17-feature-cis-volume-snapshot-beta/>`.
* The `VolumeSnapshotDataSource <https://v1-13.docs.kubernetes.io/docs/reference/command-line-tools-reference/feature-gates/>`
  feature gate must be enabled.
* The `VolumeSnapshot <https://kubernetes.io/docs/concepts/storage/volume-snapshots/>`
  API must be enabled.
* The default storage class and snapshot class must use a CSI driver with
  snapshot capabilities.

Titan currently uses the default Kubernetes config file, cluster and namespace
as defined the `.kube/config` file in your home directory. Future versions will make these
configurable.

The titan server still runs as a container on the local workstation. A local
Docker installation is required, though no special privileges or operating
system support is necessary. This also means that all the metadata is local to the
user, so two users cannot share titan repositories in a shared Kubernetes
cluster. The pods themselves will be accessible to any kubernetes user, but
there is no way to manage them as Titan repositories on a different system.

Each push or pull operation is run as a separate Job, requiring that the
`titandata/titan` image be avaialble to the cluster.

Kubernetes Architecture
-----------------------

A Kubernetes repository consists of:

* A PersistentVolumeClaim for each volume identified in the image metadata.
  These are currently always hardcoded to be 1GiB, and always use the default
  StorageClass. Each is given a unique GUID and name.
* A StatefulSet with the same name as the repository.
* Within that StatefulSet, all PersistentVolumeClaims mapped to the directories
  identified in the image metadata. The pod name is the same as the repository
  name.
* A service that maps all exposed ports to the ports of the Pods within the
  StatefulSet.

Each commit corresponds to a VolumeSnapshot.

By default, Titan will make all ports available on the local system. This
is accomplished by running `kubectl port-forward` for each known port. This
is a fairly fragile process, since that process can die or the system
restarted at any time. This will be replaced with a more reliable mechanism
in the future.

Limitations
-----------

.. attention ::

   Kubernetes support is currently in an _beta_ state. Many elements of
   configurability and reliability have not yet been fully fleshed out,
   and it may not work in all environments.

In addition to the general immaturity of Kubernetes support, there are some
specific known limitations with beta:

* There is no method to specify volume sizes. While the amount of data pushed
  and pulled will remain the logical size of the dataset, volumes must be
  statically sized in Kubernetes. Currently, these are always 1GiB.
* Titan currently always uses the default ~/.kube configuration, and there isn't
  a way to control the namespace and cluster used. If the default configuration
  is changed after the context is installed, it can result in inconsistent
  state.
* Titan will always use the default storage class and snapshot class. These
  are not currently configurable.
* There are various failure modes, such as failing to pull an image, that
  aren't handled well by Titan. These can result in hangs or hard to diagnose
  errors.
* Port forwarding is very simplistic. Titan simply spawns `kubectl port-forward`
  in the background, and tries to kill it when stopping port forwarding. If
  the system is restarted, or that process dies, it will need to be manually
  restarted, either by running the `kubectl` directly, or stopping and
  starting the repository.
