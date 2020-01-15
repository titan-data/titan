.. _lifecycle_install:

Installation and Configuration
==============================

Installing Docker
-----------------
Before installing Titan, you must have docker configured on your system and
permission to run privileged Linux containers. For MacOS and Windows, this
means installing `Docker Desktop <https://www.docker.com/products/docker-desktop>`_.
For Linux, this means `installing docker <https://docs.docker.com/v17.12/install>`_
via your distribution-specific mechanism.

If you can run a basic Linux docker container you're ready for the next step::

    $ docker run --rm busybox:latest echo ready
    ready

Downloading Titan
-----------------
To download Titan, head over to the
`Download Page <https://titan-data.io/download>`_ and download the archive
specific to your platform. Extract the archive and place it in a location that
is part of your ``PATH`` such as ``~/bin`` or ``/usr/local/bin``.

If you can get the current Titan version you're ready for the next step::

    $ titan --version
    titan version 0.4.1

Installing Titan
----------------
While Titan is delivered as a standalone executable, it relies on a
containerized service to do a lot of the heavy lifting. The ``titan install``
command will download and run these containers. It may take some time
to download the titan image, but once complete you should be able to see
two containers running named ``titan-docker-launch`` and
``titan-docker-server``:

.. note::

   **For MacOS users:** By default, MacOS will block unverified binaries (which
   this is). You may receive an error similar to "'titan' cannot be opened
   because the developer cannot be verified."

   To resolve this, click "cancel," then navigate to "System Preferences"->
   "Security and Privacy">"General" where you will see something like:
   "'Titan' was blocked from use because it's not from an identified developer."

   Click "Open Anyway,"
   return to the terminal and re-run ``titan install``

::

    $ titan install
    Initializing titan infrastructure ...
        √ Checking docker installation
        √ Starting titan server docker containers
    Titan cli successfully installed, happy data versioning :)
    $ docker ps
    CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                    NAMES
    ff80dcdf8d0e        titan:latest        "/titan/run"             9 seconds ago       Up 7 seconds        0.0.0.0:5001->5001/tcp   titan-docker-server
    6b09cccc407a        titan:latest        "/bin/bash /titan/la…"   29 seconds ago      Up 14 seconds                                titan-docker-launch

By deafult, this installs a local docker context, and is equivalent to
``titan context install -t docker``. If you want to install Titan
for use with Kubernetes, see the :ref:`lifecycle_context` and
:ref:`lifecycle_kubernetes` sections. If you are operating in a corporate
environment without access to the main docker registry, you can manually load
the ```titandata/titan`` image into a private registry and use the ``-r
registry`` option to ``titan install`` to pull from there instead.

When using the local docker context, the ``titan-<context>-launch`` container
is responsible for installing ZFS on the Docker or host VM. For more
information on how this works and supported configurations, see the
:ref:`lifecycle_docker` section.

If you can successfully run ``titan ls``, then you should be all set::

    $ titan ls
    CONTEXT             REPOSITORY             STATUS
