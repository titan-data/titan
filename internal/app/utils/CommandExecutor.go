package utils

import (
	"fmt"
	"os/exec"
)

type commandExecutor struct {
	timeout int
	debug bool
}

func CommandExecutor(timeout int, debug bool) commandExecutor {
	var t int
	var d bool
	if timeout > 0 {
		t = timeout
	} else {
		t = 60
	}
	if debug != false {
		d = debug
	} else {
		d = false
	}
	return commandExecutor{t, d}
}

func (ce commandExecutor) Exec(name string, arg ...string) (string, error) {
	if ce.debug {
		fmt.Println(name, arg)
	}
	out, err := exec.Command(name, arg...).CombinedOutput()
	return string(out), err
}