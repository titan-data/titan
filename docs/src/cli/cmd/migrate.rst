.. _cli_cmd_migrate:

titan migrate
=============

Migrate data from a running container into new Titan repository. Given a
stopped container with volumes mounted from the host system, this command
will create a new container with an identical configuration, and then
copy over the data into the new repository. For more information, see the
:ref:`local_migrate` section.

.. note::

   The migrate command will only work if the volumes have been mounted from
   host system. Migrating local to the container, or from a different volume
   driver, is not supported.

Syntax
------

::

    titan migrate <-s source> <repository>

Arguments
---------

repository
    *Required*. The name of the repository to create.

Options
-------

-s, --source container  Name of source container.

Example
-------

::

    $ titan migrate -s postgres_source hello-world
    Creating repository hello-world
    Creating docker volume hello-world/v0 with path /var/lib/postgresql/data
    Copying data to hello-world/v0
    Commit 9560ffb0-6bbc-4b1c-acb5-1142f86c0354
    postgres_source migrated to controlled environment hello-world
