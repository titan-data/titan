package common

import (
	"fmt"
	"strings"
)

type Version string

const(
	V2 Version = "v2"
	V1 Version = "v1"
)

type image struct {
	Image  string `json:"image"`
	Tag    string `json:"tag"`
	Digest string `json:"digest"`
}
type port struct {
	Protocol string `json:"protocol"`
	Port     string `json:"port"`
}
type volume struct {
	Name string `json:"name"`
	Path string `json:"path"`
}

type Metadata struct {
	version Version
	user string
	email string
	message string
	source string
	tags map[string]string
	timestamp string
	image image
	environment []interface{}
	ports []port
	volumes []volume
}

func (m *Metadata) SetUser(s string) {
	m.user = s
}

func (m *Metadata) SetEmail(s string)  {
	m.email = s
}

func (m *Metadata) SetMessage(s string) {
	m.message = s
}

func (m *Metadata) SetTags(t map[string]string) {
	m.tags = t
}

func (m *Metadata) SetSource(s string) {
	m.source = s
}

func (m Metadata) ToMap() map[string]interface{} {
	returnMap := make(map[string]interface{})
	if m.user != "" {
		returnMap["user"] = m.user
	}
	if m.email != "" {
		returnMap["email"] = m.email
	}
	if m.message != "" {
		returnMap["message"] = m.message
	}
	if m.source != "" {
		returnMap["source"] = m.source
	}
	if len(m.tags) > 0 {
		returnMap["tags"] = m.tags
	}
	if m.timestamp != "" {
		returnMap["timestamp"] = m.timestamp
	}
	if m.version == V2 {
		returnMap["v2"] = map[string]interface{} {
			"image": m.image,
			"environment": m.environment,
			"ports": m.ports,
			"volumes": m.volumes,
		}
	}
	if m.version == V1 {
		returnMap["container"] = m.image.Digest
		returnMap["image"] = m.image.Image
		returnMap["tag"] = m.image.Tag
		returnMap["digest"] = m.image.Digest
	}
	return returnMap
}

func (m Metadata) Load(metaMap map[string]interface{}) Metadata {
	_, ok := metaMap["v2"]
	if ok  {
		return m.MapV2(metaMap)
	} else {
		return m.MapV1(metaMap)
	}
}

func stringFromMap(m map[string]interface{}, k string) string {
	v, ok := m[k]
	if ok {
		return v.(string)
	} else {
		return ""
	}
}

func (m Metadata) MapV2(metaMap map[string]interface{}) Metadata {
	user := stringFromMap(metaMap, "user")
	email := stringFromMap(metaMap, "email")
	message := stringFromMap(metaMap, "message")
	source := stringFromMap(metaMap, "source")
	timestamp := stringFromMap(metaMap, "timestamp")

	tags := make(map[string]string)
	v, ok := metaMap["tags"].(map[string]interface{})
	if ok {
		for key, value := range v {
			switch value := value.(type) {
			case string:
				tags[key] = value
			}
		}
	} else {
		tags = nil
	}

	meta := metaMap["v2"].(map[string]interface{})
	imageMap := meta["image"].(map[string]interface{})
	image := image{
		Image:  fmt.Sprintf("%v", imageMap["image"]),
		Tag:    fmt.Sprintf("%v", imageMap["tag"]),
		Digest: fmt.Sprintf("%v", imageMap["digest"]),
	}

	envCheck := meta["environment"] //TODO this can be empty
	var environment []interface{}
	if envCheck != nil {
		environment = meta["environment"].([]interface{})
	} else {
		environment = nil
	}

	metaPorts := meta["ports"].([]interface{})
	var ports []port
	for _, v := range metaPorts{
		mapPort := v.(map[string]interface{})
		ports = append(ports, port{
			Protocol: fmt.Sprintf("%v", mapPort["protocol"]),
			Port:     fmt.Sprintf("%v", mapPort["port"]),
		})
	}
	var volumes []volume
	metaVols := meta["volumes"].([]interface{})
	for _, v := range metaVols {
		metaVol := v.(map[string]interface{})
		volumes = append(volumes, volume{
			Name: fmt.Sprintf("%v",metaVol["name"]),
			Path: fmt.Sprintf("%v",metaVol["path"]),
		})
	}

	return Metadata{
		version:     V2,
		user:        user,
		email:       email,
		message:     message,
		source:      source,
		tags:        tags,
		timestamp:   timestamp,
		image:       image,
		environment: environment,
		ports:       ports,
		volumes:     volumes,
	}
}

func (m Metadata) MapV1(metaMap map[string]interface{}) Metadata {
	user := stringFromMap(metaMap, "user")
	email := stringFromMap(metaMap, "email")
	message := stringFromMap(metaMap, "message")
	source := stringFromMap(metaMap, "source")
	timestamp := stringFromMap(metaMap, "timestamp")

	var tags map[string]string
	v, ok := metaMap["tags"]
	if ok {
		tags = v.(map[string]string)
	} else {
		tags = nil
	}

	digest := stringFromMap(metaMap, "container")
	var imageName string
	v, ok = metaMap["Image"]
	if ok {
		imageName = v.(string)
	} else {
		if strings.Contains(digest, "@") {
			imageName = strings.Split(digest, "@")[0]
		} else {
			imageName = strings.Split(digest, ":")[0]
		}
	}
	var imageTag string
	v, ok = metaMap["Tag"]
	if ok {
		imageTag = v.(string)
	} else {
		imageTag = ""
	}
	image := image{
		Image:  imageName,
		Tag:    imageTag,
		Digest: digest,
	}

	runtimeString := stringFromMap(metaMap, "runtime")
	runtimeString = strings.TrimLeft(runtimeString, "[")
	runtimeString = strings.TrimRight(runtimeString, "]")
	runtime := strings.Split(runtimeString, " ")
	for i, n := range runtime{
		if strings.Contains(n,"--mount") {
			runtime = append(runtime[:i], runtime[i+1:]...)
		}
		if strings.Contains(n,"type=volume"){
			runtime = append(runtime[:i], runtime[i+1:]...)
		}
	}

	var envs []interface{}
	var ports []port
	var volumes []volume

	for i, n := range runtime {
		if strings.Contains(n, "--env") || strings.Contains(n, "-e") {
			envs = append(envs, runtime[i+1])
		}
		if n == "-p" {
			var p string
			if strings.Contains(runtime[i+1], ":"){
				p = strings.Split(runtime[i+1], ":")[1]
			} else {
				p = runtime[i+1]
			}
			ports = append(ports, port{
				Protocol: "tcp",
				Port:     p,
			})
		}
		if n == "--mount" {
			vols := strings.Split(runtime[i+1], ",")
			var name string
			var path string
			for _, vol := range vols {
				sv := strings.Split(vol, "=")
				if sv[0] == "src" {
					name = strings.Split(sv[1], "/")[1]
				}
				if sv[0] == "dst" {
					path = sv[1]
				}
			}
			volumes = append(volumes, volume{
				Name: name,
				Path: path,
			})
		}
	}
	return Metadata{
		version:     V1,
		user:        user,
		email:       email,
		message:     message,
		source:      source,
		tags:        tags,
		timestamp:   timestamp,
		image:       image,
		environment: envs,
		ports:       ports,
		volumes:     volumes,
	}
}
