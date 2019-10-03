.. _remote_clone:

Cloning Repositories
====================

The :ref:`cli_cmd_clone` command will create a new repository using the
configuration from a remote. It is equivalent to creating a new repository with
an identical configuration, adding the remote, and pulling down the latest
commit::

    $ titan clone s3://titan-data-demo/hello-world/postgres hello-world

The docker configuration is persisted with each commit, so the local repository
uses whatever the configuration was as of the last commit.

.. note::

   There is not currently any way to override the docker configuration, such
   as wanting to use a different port or network configuration. This
   capability will be added in a future release.

.. note::

   The clone command currently always uses the latest commit by default. To clone a specific
   commit, add the commit GUID to the URI with a `#` tag. Example::

    $ titan clone s3://titan-data-demo/hello-world/postgres#0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f hello-world

