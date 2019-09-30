.. _lifecycle_uninstall:

Uninstalling Titan
==================

The :ref:`cli_cmd_install` command will install supporting Titan infrastructure
automatically, including installing ZFS on the host or Docker VM if necessary.
The :ref:`cli_cmd_uninstall` command will uninstall titan, destroying any
repositories in the process.

.. warning::

   Uninstalling titan will remove all repositories. This operation cannot be
   undone.

If Titan was responsible for installing ZFS on the host VM, it will also
uninstall ZFS. If ZFS was already present on the system when Titan was
installed, then it will leave the ZFS installation as-is.

.. note::

   The process only uninstalls the supporting infrastructure. You will have to
   manually remove the ``titan`` binary yourself.
