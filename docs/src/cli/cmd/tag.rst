.. _cli_cmd_tag:

titan tag
=========

Add or modify tags for a local commit. While tags can be specified when a
commit is created, this command can be used to update those tags after the
fact.

Syntax
------

::

    titan tag -c <id> [-t key[=value] ...] <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-c, --commit id         *Required*. Specify the commit ID to update. Must be a
                        known commit in `titan log` for the given repository.

-t, --tag tag           Specify the tag(s) to be added or modified. More than
                        one of this option can be specified. If present, then
                        the tag is replaced (if the key exists) or added (if
                        the key does not exist). For more information, see the
                        :ref:`local_tags` section.

Example
-------

::

    $ titan tag -c 7715327e95354263870ff5c92c18cb23 -t foo -t bar=baz myrepo
    7715327e95354263870ff5c92c18cb23 deleted
