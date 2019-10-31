.. _cli_cmd_upgrade:

titan upgrade
=============

Upgrade the titan software on the host system. This will automatically fetch
the latest version of Titan, replace the current titan binary, and then
update the titan supporting infrastructure. For more information on upgrade,
see the :ref:`lifecycle_upgrade` section.

.. warning::

   Upgrade requires all containers to be stopped, or the '-f' option to
   forcefully stop all containers.

Syntax
------

::

    titan upgrade [-f] [-p path]

Options
-------

-f, --force     Stop all containers.

-p, --path      Specify path to titan binary. By default, will attempt to find
                the binary in the path. If you are executing titan as an
                alias, or in a wrapper script, you will need to specify the
                path to the actual titan binary.
