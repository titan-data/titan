.. _cli_cmd_install:

titan install
=============

Installs required titan infrastructure. Must be run prior to any other titan
commands. For more information on how to install titan and what's required, see
the :ref:`lifecycle_install` section. This command will not do anything if titan
is already installed on the system.

Syntax
------

::

    titan install

Options
-------

-r, --registry  registry    Docker Registry URL for private repositories.
                            Defaults to titandata from docker hub.
-V, --verbose               Optionally output titan-server install details.

Example
-------

::

    $ titan install -V -r your.registry.address:port
