.. _cli_cmd_delete:

titan delete
==============

Delete a previous commit from the current container. The commit must be
present in `titan log`. For more general information on managing local
commits, see the :ref:`local_commit` section.

.. warning::

   This will stop and start the associated docker container if it is already
   running. This will interrupt any active connections, and may require
   client-specific actions to reconnect.

Syntax
------

::

    titan delete -c <id> <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.

Options
-------

-c, --commit id   *Required*. Specify the commit ID to delete. Must be a known
                  commit in `titan log` for the given repository.

Example
-------

::

    $ titan delete -c 7715327e-9535-4263-870f-f5c92c18cb23 myrepo
    7715327e-9535-4263-870f-f5c92c18cb23 deleted
