.. _cli_cmd_context_ls:

titan context ls
================

Lists configured Titan contexts. This configuration is read from the
``~/.titan/config`` file. For more information on managing Titan
contexts, see the :ref:`lifecycle_context` section.

For each context, the command will display:

* The context name. If the context is the default context, then an additional
  " (*)" will be appended to the name.
* The context type. One of "docker" or "kubernetes"
* The context configuration. For more information on context configuration,
  see the :ref:`lifecycle_docker` and :ref:`lifecycle_kubernetes` sections.

Syntax
------

::

    titan context ls

Example
-------

::

    $ titan context ls
    NAME                  TYPE          CONFIGURATION
    kubernetes (*)        kubernetes    titanImage=titandata/titan:0.7.0
    docker                docker        pool=titan-docker
