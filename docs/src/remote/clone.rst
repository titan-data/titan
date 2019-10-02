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

   The clone command currently always uses the latest commit. The ability to
   select a specific commit to use will be added in a future release.
