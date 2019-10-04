.. _cli_cmd_remote_ls:

titan remote ls
===============

List remotes for the given repository. For more information on managing remotes,
see the :ref:`remote_addremove` section.

Syntax
------

::

    titan remote ls <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Example
-------

::

    $ titan remote ls hello-world
    REMOTE                PROVIDER
    origin                s3
