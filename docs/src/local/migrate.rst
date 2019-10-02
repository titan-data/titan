.. _local_migrate:

Migrating Existing Containers
=============================

Titan provides lots of freedom for how you might populate data into a new
repository. You might run some scripts to generate data, load data from
a database dump, or replicate data into the container. Titan does not try to
encode these database-specific mechanisms into the tool, but does provide some
platform-agnostic capabilities for loading data. In this section, we show
how you can migrate data from a container you already have running on your
laptop.

Source Container
----------------
The source container must be from an image available in a docker registry,
and the data corresponding to volume directives (see the :ref:`local_copy`
section) must be mounted from your host system. For example, you must have
run::

    docker run -d -v /home/henrietta/postgres/data:/var/lib/postgresql/data postgres:11

If you didn't specify a ``-v`` value, or used a volume not mounted from the
host system, thn ``migrate`` won't work.

The container must also be stopped. This is both to ensure that the underlying
data is not changing, and because the new repository will use the same
configuration, potentially creating conflicts with ports and other global
resources.

Migrating a Container
---------------------
Assuming the container meets the above criteria, you can create a repository
from it by running::

    titan mgirate -s somecontainer myrepo

This will get the configuration of the container and use it verbatim for the
new repository. It will determine where each volume resides on the host
system, and use the equivalent of :ref:`cli_cmd_cp` to copy the volumes over
one by one.

.. note::

   There is no way to alter the configuration of the container at the time
   it is migrated, you must use the identical configuration as the source
   container.
