.. _cli_cmd_cp:

titan cp
========

Copies data from an existing directory on the host into a titan repository.
Each container has data at a known place, such as ``/var/lib/data/postgresql``
for PostgreSQL. This command allows users to copy data created by another
instance of the database into the titan repository.

If there is only one volume then the destination is not required. If there
is more than one volume, then you must specify the destination within the
repository container. For more information on copying data into repositories,
including how to determine what destinations are available, see the
:ref:`local_copy` section.

Syntax
------

::

    titan cp <-s source> [-d destination] <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-s, --source dir        *Required*. Source directory on the host system. This
                        must match the layout and contents expected by the
                        container.

-d, --destination dir   Required if there is more than volume associated with
                        the repository. Specifies the path where data should
                        be copied to within the repository. Must correspond
                        to a volume path in the repository container.

Example
-------

::

    $ titan cp -s /var/postgres-data/ hello-world
    hello-world stopped
    Copying data to hello-world/v0
    hello-world started
    hello-world running with data from /var/postgres-data
