.. _cli_cmd_push:

titan push
==========

Push a commit to a remote repository. For more information on pushing
commits, see the :ref:`remote_pushpull` section.

Syntax
------

::

    titan push [-c commit] [-r remote] <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-c, --commit commit     Commit to push. If not specified, then the latest
                        commit is used.

-r, --remote remote     Name of remote to push to. If not specified, defaults
                        to "origin".

Example
-------

::

    $ titan push hello-world
    PUSH 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f to ssh RUNNING
    Pushing 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f to 'origin'
    Syncing /var/lib/postgresql/data
    21.10MB (17.07MB/s)PUSH 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f to ssh COMPLETE
