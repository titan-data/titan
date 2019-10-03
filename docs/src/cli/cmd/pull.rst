.. _cli_cmd_pull:

titan pull
==========

Pull a commit from a remote repository. For more information on pushing
commits, see the :ref:`remote_pushpull` section.

Syntax
------

::

    titan pull [-c commit] [-r remote] <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-c, --commit commit     Commit to pull. If not specified, then the latest
                        commit is used.

-r, --remote remote     Name of remote to push to. If not specified, defaults
                        to "origin".

Example
-------

::

    $ titan pull hello-world
    PULL 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f from origin RUNNING
    Pulling 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f from 'origin'
    PULL 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f from origin COMPLETE
