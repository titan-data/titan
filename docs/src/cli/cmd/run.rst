.. _cli_cmd_run:

titan run
=========

Runs a new repository. This command will process all arguments after ``--``` as
arguments to ``docker run``. For more information on running repositories, see
the :ref:`local_run` section.

Syntax
------

::

    titan run [OPTIONS] IMAGE -- [additional context specific arguments]...

Arguments
---------

IMAGE
    *Required*. The container image to run.

Options
-------

-P, --disable-port-mapping      Default: false. Disable the automatic specific
                                port mapping of exposed ports from the container
                                to localhost.

-e, --env TEXT                  Environment variables for the container being run.
                                Examples: PGPASSWORD,  MONGO_INITDB_ROO_PASSWORD

-n, --name TEXT                 Optional new container name. If not provided,
                                container name will be the same as the IMAGE.

Example
-------

::

    $ titan run -n newMongo mongo
    Creating repository newMongo
    Creating docker volume newMongo/v0 with path /data/configdb
    Creating docker volume newMongo/v1 with path /data/db
    Running controlled container newMongo
