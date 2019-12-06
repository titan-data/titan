.. _cli_cmd_context_default:

titan context default
=====================

Get or set the default titan context. For more information on managing contexts,
see the :ref:`lifecycle_context` section,

Syntax
------

::

    titan context default [context]

Arguments
---------

context
    Optional. If specified the named context is made the default context.
    Otherwise, the current default context is returned.

Example
-------

::

    $ titan context default
    kubernetes
    $ titan context default mycontext
    $ titan context default
    mycontext
