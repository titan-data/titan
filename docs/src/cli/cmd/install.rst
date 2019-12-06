.. _cli_cmd_install:

titan install
=============

Installs the default Titan context if not context is currently configured,
equivalent to ``titan context install -t docker``. Must be run prior to any
other titan commands. For more information on how to install titan and what's
required, see the :ref:`lifecycle_install` section. For more information on
managing contexts, see the :ref:`lifecycle_context` section. If you want to
install Titan for kubernetes, see the :ref:`lifecycle_kubernetes` section. This
command will not do anything if titan is already installed on the system.

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
