.. _cli_cmd_uninstall:

titan uninstall
===============

Uninstall all supporting titan infrastructure. This will remove the titan
containers and clean up any resources on the docker or host VM, leaving
the titan CLI binary in place. For more information on what exactly is
cleaned up, see the :ref:`lifecycle_uninstall` section. This command will fail
if any active repositories exist, unless the ``-f`` option is supplied.

.. warning::

   If you specify the ``-f`` force option, *all* titan repositories will be
   forcibly destroyed. This action is not recoverable. Proceed with caution.

Syntax
------

::

    titan uninstall [-f]

Options
-------

-f, --force     Forcibly stop and remove any repositories that currently exist.
