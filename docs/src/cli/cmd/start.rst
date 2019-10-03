.. _cli_cmd_start:

titan start
===========

Starts the container associated with the repository. Equivalent to
``docker start``.

Syntax
------

::

    titan start <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Example
-------

::

    $ titan start hello-world
    hello-world started
