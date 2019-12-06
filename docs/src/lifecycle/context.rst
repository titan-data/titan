.. _lifecycle_context:

Managing Titan Contexts
=======================

All titan repositories are associated with a single context. Each context
has a type, either Docker or Kubernetes, that defines how it manages
repositories within it.

* Docker contexts manage containers locally on the current workstation.
* Kubernetes contexts manage containers within a Kubernetes cluster.

In both cases, there is a local container that stores the metadata associated
with the repositories and orchestrates their lifecycle. These containers are
named ``titan-(context)-server`` and, for local docker contexts,
``titan-(context)-launch``.

Context Configuration
---------------------
Contexts are installed through the :ref:`cli_cmd_context_install` command.
Each context has:

* A type ("docker" or "kubernetes")
* A name (defaults to the type of not specified)
* Optional context-specific parameters

The :ref:`cli_cmd_install` command is an alias for
``titan context install -t docker``, and will create a default docker context
for managing local containers.

The context configuration is stored in the ``.titan/config`` file in your
home directory. This is a YAML file that contains a ``contexts`` object that
is a map of context configurations, with the key being the name of the context
and the fields the following:

* ``host`` - Host to connect to. Currently always "localhost"
* ``type`` - Type of the context. One of "docker" or "kubernetes"
* ``port`` - Port that the context container is listening on. Selected at
  random when the context is installed.

While this file can be edited by hand, it is recommend to use the Titan context
commands. To list available contexts, use the :ref:`cli_cmd_context_ls`
command. To uninstall a context, use the :ref:`cli_cmd_context_uninstall`

Selecting Contexts
------------------
In most situations, a single Titan context is sufficient. When a single
context is in place, repositories can simply be referenced by their name,
and any new repository is created within that context.

Repositories can also be referenced by their fully qualified name,
``<context>/<repository>``. This can be used to uniquely identify any repository,
even when there are multiple contexts are configured. This can also be used to
select which context to use when creating a new repository, such as
``titan run mongo -n contextone/mongo``.

If the context is not specified, but there is more than one context configured,
Titan will attempt to determine the appropriate context in one of two ways:

* If referencing an existing repository, as opposed to creating a new
  repository, then Titan will try to find a repository with the matching
  name, but will generate an error if repositories with multiple names
  exist.
* If creating a new repository, then the default context (as noted in the
  context configuration file) is used.

The default context is identified in ``titan context ls`` output via a
" (*)" indicator. You can also get the default context with the
:ref:`cli_cmd_context_default` command. To set the default context, run
``titan context default <name>``.
