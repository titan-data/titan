.. _cli_cmd_context_uninstall:

titan context uninstall
=======================

Uninstall a titan context. This will permanently remove the context and any
associated repositories (if '-f' is specified). For more information about
contexts, see the :ref:`lifecycle_context` section.

Syntax
------

::

    titan context uninstall [-f] context

Arguments
---------

context
    *Required*. The name of the target context.

Options
-------

-f, --force              Force the removal of any repositories in the context.
                         By default, the command will fail if any repositories
                         exist.

Example
-------

::

    $ titan context uninstall newcontext
    Removing Titan Docker volume 100% │███████████████████████████████████│ 100/100 (0:00:00 / 0:00:00)
    Uninstalled titan infrastructure
