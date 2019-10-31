.. _cli_cmd_pull:

titan pull
==========

Pull a commit from a remote repository. For more information on pushing
commits, see the :ref:`remote_pushpull` section.

Syntax
------

::

    titan pull [-c commit] [-r remote] [-t key[=value] ...] <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-c, --commit commit     Commit to pull. If not specified, then the latest
                        commit is used unless tags are specified, in which
                        case the latest tag matching the given tags is used.

-r, --remote remote     Name of remote to push to. If not specified, defaults
                        to "origin".

-t, --tag tag           Filter commits by the specified tag(s).
                        More than one of this option can be specified. If
                        present, then the last commit matching the given tags
                        is used. This is incompatible with the ``-c`` option.
                        Tags are matched according to the filtering rules
                        described in the :ref:`local_tags` section.

Example
-------

::

    $ titan pull hello-world
    PULL 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f from origin RUNNING
    Pulling 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f from 'origin'
    PULL 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f from origin COMPLETE
