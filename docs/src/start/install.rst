.. _start_install:

Installation and Configuration
==============================

For a high-level quick start, see the
`Getting Started <https://titan-data.io/getting-started>`_ guide on the
community site.

Installing Docker
-----------------
Before installing Titan, you must have docker configured on your system and
permission to run privileged Linux containers. For MacOS and Windows, this
means installing `Docker Desktop <https://www.docker.com/products/docker-desktop>`_.
For Linux, this means `installing docker <https://docs.docker.com/v17.12/install>`_
via your distribution-specific mechanism.

If you can run a basic Linux docker container you're ready for the next step::

    docker run --rm busybox:latest echo ready

Downloading Titan
-----------------
To download Titan, head over to the
`Download Page <https://titan-data.io/download>`_ and download the archive
specific to your platform. Extract the archive and place it in a location that
is part of your ``PATH`` such as ``~/bin`` or ``/usr/local/bin``.

If you can get the current Titan version you're ready for the next step::

    $ titan --version
    titan version 0.3.0

Installing Titan
----------------
While Titan is delivered as a standalone executable, it relies on a
containerized service to do a lot of the heavy lifting. The ``titan install``
command will download and run these containers. It may take some time
to download the titan image, but once complete you should be able to see
two containers running named ``titan-launch`` and ``titan-server``::

    $ titan install
    $ titan install
    Initializing titan infrastructure ...
    	√ Checking docker installation
    	√ Starting titan server docker containers
    Titan cli successfully installed, happy data versioning :)
    $ docker ps
    CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                    NAMES
    ff80dcdf8d0e        titan:latest        "/titan/run"             9 seconds ago       Up 7 seconds        0.0.0.0:5001->5001/tcp   titan-server
    6b09cccc407a        titan:latest        "/bin/bash /titan/la…"   29 seconds ago      Up 14 seconds                                titan-launch

If you are operating in a corporate environment without access to the main
docker registry, you can manually load the ```titandata/titan`` image into
a private registry and use the ``-r registry`` option to ``titan install``
to pull from there instead.

Among other things, the ``titan-launch`` container is responsible for installing
ZFS on the Docker or host VM. This is the primary area where
:ref:`lifecycle_supported` comes into play. For Windows and MacOS, docker is
running on the same HyperKit VM, and so it is relatively easy for the
community to keep delivering pre-built ZFS binaries for those installations.
With Linux, however, the story is much different as there are a wide variety
of distributions, each with their own mechanism of fetching required
dependencies to build ZFS. For the distributions noted in the
:ref:`lifecycle_supported` list, we attempt to keep up-to-date with the
latest releases.

If we do not have a pre-built version of the ZFS binaries, we will attempt to
build them on the fly. For Linux, we are still limited to the set of supported
distributions, but we can built for slightly different variations or versions
if needed. If you are running a Linux system other than a supported
distribution, you can also compile and install ZFS yourself, provided it's
version `0.8.1`, and Titan will use that instead of trying to install its own.

If the installation is taking a while, and you see a ``zfs-builder``
container in ``docker ps`` output, then it's off building a custom version
of ZFS. If you are running a supported operating system, then reach out to the
community to see if new pre-built binaries need to be created.

If you can successfully run ``titan ls``, then you should be all set::

    $ titan ls
    CONTAINER             STATUS
