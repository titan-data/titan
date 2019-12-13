.. _cli_cmd_uninstall:

titan uninstall
===============

Uninstall all titan infrastructure. This will uninstall all contexts,
equivalent to ``titan context uninstall``. It will then remove any titan images
pulled locally, as well as the ``~/.titan`` directory. For more information on
what exactly is cleaned up, see the :ref:`lifecycle_uninstall` section. This
command will fail if any active repositories exist, unless the ``-f`` option is
supplied.

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
