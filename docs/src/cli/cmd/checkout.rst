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

    titan checkout -c <id> <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-c, --commit id   *Required*. Specify the commit ID to checkout. Must be a known
                  commit in `titan log` for the given repository.

Example
-------

::

    $ titan checkout -c 7715327e-9535-4263-870f-f5c92c18cb23 myrepo
    Stopping container myrepo
    Checkout 7715327e-9535-4263-870f-f5c92c18cb23
    Starting container myrepo
    7715327e-9535-4263-870f-f5c92c18cb23 checked out
