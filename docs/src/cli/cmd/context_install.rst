.. _cli_cmd_context_install:

titan context install
=====================

Install a new Titan context. For more information about contexts, see the
:ref:`lifecycle_context` section.

Syntax
------

::

    titan context install [-t type] [-n name] [-p parameter=value ...] [-v]

Options
-------

-t, --type       type    Optional context type. Must be one of "docker" or
                         "kubernetes". Defaults to "docker".

-n, --name       name    Optional context name. Must be unique. Defaults to
                         the type of the context ("docker" or "kubernetes").

-p, --parameters string  Key=Value pair for provider specific options. See
                         the context-specific documentation for more information.

-v, --verbose            Enable verbose logging. Some contexts can provide
                         additional information about the installation
                         process.

Example
-------

::

    $ titan context install -t kubernetes -n newcontext
    Initializing titan infrastructure ...
    Checking docker installation 100% │███████████████████████████████████│ 100/100 (0:00:00 / 0:00:00)
    Starting titan server docker containers 100% │████████████████████████│ 100/100 (0:00:15 / 0:00:00)
    Titan cli successfully installed, happy data versioning :)
