package app

import (
	"strconv"
	"strings"
)

type Version struct {
	major int
	minor int
	micro int
}

func (Version) FromString(version string) Version {
	v := strings.Split(version, ".")
	major, _ := strconv.Atoi(v[0])
	minor, _ := strconv.Atoi(v[1])
	micro, _ := strconv.Atoi(v[2])
	return Version{major, minor, micro}
}

func (from Version) Compare(to Version) int {
	if from.major > to.major {
		return 1
	}
	if from.major < to.major {
		return -1
	}
	if from.minor > to.minor {
		return 1
	}
	if from.minor < to.minor {
		return -1
	}
	if from.micro > to.micro {
		return 1
	}
	if from.micro < to.micro {
		return -1
	}
	return 0
}