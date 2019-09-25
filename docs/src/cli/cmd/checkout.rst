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

::

    titan checkout [-c commit] <repository>

The following options are supported:

-c, --commit    Optional. Specify the commit ID to checkout. Must be a known
                commit in `titan log` for the given repository. If not
                specified, then the latest commit (based on commit timestamp)
                will be used.
