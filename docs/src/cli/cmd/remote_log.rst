.. _cli_cmd_remote_log:

titan remote log
================

List commits in a remote. For more information on managing remotes, see
the :ref:`remote` section.

Syntax
------

::

    titan remote log [-r remote] [-t key[=value] ...] <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-r, --remote remote     Optional remote name. If not provided, then the name
                        'origin' is assumed.

-t, --tag tag           Filter commits by the specified tag(s).
                        More than one of this option can be specified. If
                        present, then only tags that match the given tags will
                        be displayed. Tags are matched according to the
                        filtering rules described in the :ref:`local_tags`
                        section.

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
