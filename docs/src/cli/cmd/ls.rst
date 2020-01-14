.. _cli_cmd_ls:

titan ls
========

List all repositories. This displays the repository name, context and
status for each. The status can be one of:

* ``running`` - The associated docker container is currently running. It can
  be stopped by running :ref:`cli_cmd_stop`.
* ``exited`` - The associated docker container exited and is not currently
  running. It can be started again by running :ref:`cli_cmd_start`.
* ``detached`` - There is no associated docker container. It may have been
  removed outside of titan by running ``docker rm``, or it may have failed
  to launch. It can be started again by running :ref:`cli_cmd_start`.

Syntax
------

::

    titan ls

Example
-------

::

    $ titan ls
    CONTEXT            REPOSITORY            STATUS
    docker             myrepo                running
    docker             myrepo2               exited
