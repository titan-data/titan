.. _cli:

Command Line Reference
======================

The ``titan`` command line is the primary tool for managing repositories and
commits. While there are a number of detailed subcommands, there are some
global options as well.

.. note::

  The ``--help`` option can be used to provide more detail about subcommands
  and their options, such as ``titan run --help`` or ``titan remote --help``.

Syntax
------

::

   titan --help
   titan --version
   titan subcommand ...

Options
-------

--version       Display the titan version and exit.
--help, -h      Display available subcommands.

Subcommands
-----------

.. toctree::
   :maxdepth: 1

   cmd/abort
   cmd/checkout
   cmd/clone
   cmd/commit
   cmd/cp
   cmd/install
   cmd/log
   cmd/ls
   cmd/migrate
   cmd/pull
   cmd/push
   cmd/remote_add
   cmd/remote_log
   cmd/remote_ls
   cmd/remote_rm
   cmd/rm
   cmd/run
   cmd/start
   cmd/status
   cmd/stop
   cmd/uninstall
   cmd/upgrade
