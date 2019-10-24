.. _local_storage:

Managing Local Storage
======================

All of the local titan storage, including the data stored on repositories,
is kept in a single docker volume ``titan-data``. This volume will persist
even across restarts of the titan infrastructure, upgrades of docker, and
other changes on the host.

By default, the ``titan-data`` volume is created as a vanilla docker volume,
which uses storage locally on the host system. If you want to use different
storage for your titan work, you can manually create the ``titan-data``
volume yourself prior to running :ref:`cli_cmd_install`.

.. warning::

   Do not manually change the contents of the ``titan-data`` volume, and do
   not change the volume on a running system; use ``titan uninstall`` first.
   Changing the contents of this volume can have unpredictable effects on
   Titan.

.. warning::

   If you do create your own ``titan-data`` volume, be aware that it will
   automatically destroyed when ``titan uninstall`` is run. There is not
   currently a way to uninstall titan while preserving the underlying
   volume.

Managing Storage Usage
----------------------
Titan does not currently support showing the space used by repositories
or by individual commits. And while removing repositories is supported,
deleting commits is not. These capabilities will be added in a future
release.
