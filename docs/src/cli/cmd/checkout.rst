.. _cli_cmd_checkout:

titan checkout
==============

Checks out a previous commit into the current container. The commit must be
present in `titan log`. For more general information on managing local
commits, see the :ref:`local_commit` section. For more information on how to
pull commits from remote repositories, see the :ref:`remote` section.

.. warning::

   This will stop and start the associated docker container if it is already
   running. This will interrupt any active connections, and may require
   client-specific actions to reconnect.

Syntax
------

::

    titan checkout [-c id] [-t key[=value] ...] <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-c, --commit id         Specify the commit ID to checkout. Must be a
                        known commit in `titan log` for the given repository.
                        If this is not specified, then the last commit
                        is used, unless tags are specified in which case the
                        latest matching commit is used instead.

-t, --tag tag           Filter commits by the specified tag(s).
                        More than one of this option can be specified.
                        Tags cannot be used if the ``-c`` option is
                        specified. When tags are specified, then the latest
                        commit matching those tags is checked out.
                        Tags are matched according to the filtering rules
                        described in the :ref:`local_tags` section.

Example
-------

::

    $ titan checkout -c 7715327e-9535-4263-870f-f5c92c18cb23 myrepo
    Stopping container myrepo
    Checkout 7715327e-9535-4263-870f-f5c92c18cb23
    Starting container myrepo
    7715327e-9535-4263-870f-f5c92c18cb23 checked out
