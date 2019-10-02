.. _cli_cmd_rm:

titan rm
========

Removes a repository. This will stop the container and destroy any local data.
This operation cannot be undone.

Syntax
------

::

    titan rm [-f] <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Example
-------

::

    $ titan rm -f hello-world
    Removing container hello-world
    Deleting volume hello-world/v0
    hello-world removed
