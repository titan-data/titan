.. _remote_clone:

Cloning Repositories
====================

The :ref:`cli_cmd_clone` command will create a new repository using the
configuration from a remote. It is equivalent to creating a new repository with
an identical configuration, adding the remote, and pulling down the latest
commit::

    $ titan clone -n hello-world s3web://demo.titan-data.io/hello-world/postgres

The docker environment is persisted with each commit, but runtime parameters are
not and can be specified with the ``--``` argument flag. See :ref:`cli_cmd_clone`
for more details.

.. note::

   The clone command currently always uses the latest commit by default. To clone a specific
   commit, add the commit GUID to the URI with a `#` tag. Example::

    $ titan clone -n hello-world s3web://demo.titan-data.io/hello-world/postgres#0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f
