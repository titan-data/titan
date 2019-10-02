.. _cli_cmd_stop:

titan stop
==========

Stops the container associated with the repository. Equivalent to
``docker stop``.

Syntax
------

::

    titan stop <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Example
-------

::

    $ titan stop hello-world
    hello-world stopped
