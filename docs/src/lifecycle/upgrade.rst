.. _lifecycle_upgrade:

Upgrading Titan
===============

Titan can be automatically upgraded with the :ref:`cli_cmd_upgrade` command.
This command works by:

1. Downloading a newest version of the titan binary
2. Finding the location of the ``titan`` binary in your ``PATH``.
3. Copying over the new titan binary
4. Running post-installation phase of the new binary, which may stop
   all repositories and upgrade the titan docker container in the process.

This will require access to download binaries from GitHub.
If any parts of this fail, it should leave the original titan installation
intact.

.. warning::

   As of version ``0.3.1``, upgrade is not currently working. This will be
   addressed in a future release.

.. note::

   If your titan binary is not found in the PATH, you can specify the
   ``--path`` option to point to where titan can be found.

Manual Upgrade
--------------
Titan does not currently support upgrading to a titan binary that has been
manually downloaded, such as when corporate firewalls prevent titan from
automatically downloading from GitHub. Until this is supported, you will have
to uninstall and re-install titan, destroying any active repositories in the
process.
