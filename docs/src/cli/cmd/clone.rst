.. _cli_cmd_clone:

titan clone
===========

Clones a new repository based on the latest commit from a remote repository.
The docker image, configuration, and data is all derived from that commit,
so it's not currently possible to specify a different docker configuration
than what was used when creating the commit. For more information on managing
the docker configuration, see the :ref:`local_docker` section.

Syntax
------

::

    titan clone [-c id] [-p key=value ...] <uri> [repository]

Arguments
---------

uri
    *Required*. The URI of the remote to clone from. For more information on
    remotes, the URI format, and different remote providers, see the
    :ref:`remote` section.


repository
    Optional. Name of the new repository to create. If not specified, then
    the name of the original repository is used (which may or may not match
    the name used in the remote URI).

Options
-------

-c, --commit     id      Specify the commit ID to checkout.

-p, --parameters string  Key=Value pair for provider specific options.

Example
-------

::

    $ titan clone s3web://demo.titan-data.io/hello-world/postgres myrepo
    Creating repository myrepo
    Creating docker volume myrepo/v0 with path /var/lib/postgresql/data
    Running controlled container myrepo
    PULL 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f from origin RUNNING
    Pulling 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f from 'origin'
    Downloading archive for /var/lib/postgresql/data
    PULL 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f from origin COMPLETE
    Stopping container myrepo
    Checkout 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f
    Starting container myrepo
    0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f checked out
