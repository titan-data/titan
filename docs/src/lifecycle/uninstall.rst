.. _lifecycle_uninstall:

Uninstalling Titan
==================

The :ref:`cli_cmd_install` command will install supporting Titan infrastructure
automatically, including installing ZFS on the host or Docker VM if necessary.
The :ref:`cli_cmd_uninstall` command will uninstall Titan, destroying any
repositories in the process.

The uninstall process first will uninstall all configured contexts. Once that
is complete, it will remove the underlying titan container images, as well as
the contents of the ``~/.titan`` directory. If you just want to uninstall a
single context while leaving the Titan images and configuration intact, use
the :ref:`cli_cmd_context_uninstall` command.

.. warning::

   Uninstalling titan will remove all repositories. This operation cannot be
   undone.

If Titan was responsible for installing ZFS on the host VM, it will also
uninstall ZFS. If ZFS was already present on the system when Titan was
installed, then it will leave the ZFS installation as-is.

.. note::

   The process only uninstalls the supporting infrastructure. You will have to
   manually remove the ``titan`` binary yourself.
