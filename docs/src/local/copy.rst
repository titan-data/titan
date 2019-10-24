.. _local_copy:

Copying Existing Data
=====================

Titan provides lots of freedom for how you might populate data into a new
repository. You might run some scripts to generate data, load data from
a database dump, or replicate data into the container. Titan does not try to
encode these database-specific mechanisms into the tool, but does provide some
platform-agnostic capabilities for loading data. In this section, we show
how you can copy data files from an existing database installation, either from
a server or on your laptop.

Determining Database Files
--------------------------
Every database has some type of persistent data beneath it. For Titan, we
rely on the docker image to tell us what data needs to be persisted. You can
find out exactly what an image expects by running ``docker inspect <image>``
and look for the ``Volumes`` section. For example,
``docker inspect postgres:11`` shows::

    "Volumes": {
        "/var/lib/postgresql/data": {}
    },

Most databases have a single directory for persistent data, but some may have
more than one. For example, ``docker inspect mongo:latest`` shows::

    "Volumes": {
        "/data/configdb": {},
        "/data/db": {}
    },

This tells you what data is expected when running the container and (if there
are multiple volumes)

Copying Data Into a Repository
------------------------------

The :ref:`cli_cmd_cp` command is designed to copy this data into a running
repository. To do this, first start a container running. Then, use the
the ``titan cp`` command::

    titan cp -s /path/to/postgres/data mypostgres

This will take data located on your latop and copy into that repository.

.. warning::

   This command will stop and re-start any repository, which will interrupt
   any active connections.

:: warning::

   The source data must not be changing at the time this command is run, or
   else it will not result in a transactionally consistent snapshot. Be sure
   to quiesce or shut down any database that may be writing to the source
   data files prior to running the command.

.. note::

   If your repository has multiple persistent volumes, you will need to use
   the ``-d`` destination parameter to specify where the data should go,
   such as ``titan cp -s /path/to/mongo/db -d /data/db mymongo``. You may
   need to run multiple such commands if you need to migrate
