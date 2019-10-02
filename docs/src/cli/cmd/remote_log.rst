.. _cli_cmd_remote_log:

titan remote log
================

List commits in a remote. For more information on managing remotes, see
the :ref:`remote_remote` section.

Syntax
------

::

    titan remote log [-r remote] <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-r, --remote remote     Optional remote name. If not provided, then the name
                        'origin' is assumed.

Example
-------

::

    $ titan remote log hello-world
    Remote: origin
    Commit 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f
    User: Eric Schrock
    Email: Eric.Schrock@delphix.com
    Date:   2019-09-20T13:45:38Z

    demo data
