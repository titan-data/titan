.. _cli_cmd_delete:

titan delete
==============

Delete a previous commit from the current container, or tags within a previous
commit. The commit must be present in `titan log`. For more general information
on managing local commits, see the :ref:`local_commit` section.

Syntax
------

::

    titan delete -c <id> [-t key[=value] ...] <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-c, --commit id         *Required*. Specify the commit ID to delete, or commit
                        from which tags should be deleted. Must be a known
                        commit in `titan log` for the given repository.

-t, --tag tag           Specify the tag(s) to be deleted. More than one of this
                        option can be specified. If present, then only tags
                        are deleted and the commit itself remains intact. Tags
                        are matched according to the filtering rules described
                        in the :ref:`local_tags` section.

Example
-------

::

    $ titan delete -c 7715327e95354263870ff5c92c18cb23 myrepo
    7715327e95354263870ff5c92c18cb23 deleted

    $ titan delete -c 428f81caf63d4314b8f41a31aad2e8b1 -t mytag myrepo
