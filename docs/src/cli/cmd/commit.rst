.. _cli_cmd_commit:

titan commit
============

Commits the current data state of the container. When creating commits, titan
will use your git configuration to determine the name and email address to use
by running ``git config user.name`` and ```git config user.email``. If
you have not configured git before, you can run
``git config --global user.name <name>`` and
``git config --global user.email <email>`` to set these values.

.. note::

   If you are not a git user and don't want to have to install it to use Titan,
   join the `Community <https://titan-data.io/community>`_ to help
   design and implement an alternative.

.. warning::

   Titan assumes that it is safe to snapshot the current state of the data
   while the container is running, and that starting the container with
   data in such a state will automatically recover. This would necessarily be
   true for any data store that can survive an unexpected outage. If you
   are working with a container that first must be manually quiesced, you
   should `titan stop` the container prior to committing state, and
   `titan start` it afterwards.

::

    titan commit [-m message] <repository>

The following options are supported:

-m, --message    Specify a human-readable message associated with the commit.
                 This message, along with author information, will be visible
                 in ``titan log`` output and propagate with the commit when
                 pushed to, or pulled from, remote repositories.
