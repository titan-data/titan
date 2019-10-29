.. _remote_addremove:

Adding and Removing Remotes
===========================

Each repository can have zero or more remotes configured. To add a remote,
use :ref:`cli_cmd_remote_add`::

    $ titan remote add s3://bucket/path myrepo

Remotes are specified as URIs, with the first portion defining the provider
(s3 in the above case), and the rest being specific to that provider. By
default, the remote is named `origin`, but you can also assign remotes
names (required when you have more than one remote). Individual parameters
for each provider can be supplied with the `-p` option, i.e. the ssh provider
can optionally use an `sshKey` parameter instead of the password in the URI.

To get a list of remotes, use :ref:`cli_cmd_remote_ls`::

    $ titan remote ls hello-world
    REMOTE                PROVIDER
    origin                s3

Remotes can be removed with the :ref:`cli_cmd_remote_rm` command.
