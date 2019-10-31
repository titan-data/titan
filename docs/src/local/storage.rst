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
To view the amount of space consumed by a repository, run the
:ref:`cli_cmd_status` command. This will display output similar to::

    $ titan status hello-world
                Status:  running
     Uncompressed Size:  526.5 KiB
       Compressed Size:  254 KiB
           Last Commit:  12c6da4d57004d3497afca4fb914ed58

    Volume                          Uncompressed  Compressed
    /var/lib/postgresql/data        31.7 MiB      6.9 MiB

The compressed size shows the amount of space currently consumed by the
repository, and the amount of space that would be freed if it were to be
destroyed. The volume size represents the amount of data actively being
used. While it can be reduced by freeing up data within the directory,
it may or may not reduce overall data consumption as that data may be
referenced by previous commits.

There is not currently a way to view the amount of storage used by individual
commits.
