.. _lifecycle_docker:

Titan with Docker
=================

Titan for docker is designed to run on any system that supports docker, but
there are some dependencies that limit the set of supported operation systems,
especially on Linux.

To help understand why this is necessary, it helps to understand a bit about the
architecture of Titan. To make titan possible, there is a container
(``titan-<context>-server``) running in the background that provides data
versioning capabilities on top of `ZFS <http://openzfs.org>`_. This requires
that ZFS be installed on the host operating system, but because of how
out-of-tree kernel modules work, this needs to be done by the titan software (a
container named ``titan-<context>-launch`` in particular). Titan attempts to
provide pre-built versions for common OSes, as well as a means to build them
on the fly for new versions, but there are limits to this system. If you are
not on a supported operating system, you may find ``titan install`` taking a
long time to build binaries, or failing outright.

If we do not have a pre-built version of the ZFS binaries, we will attempt to
build them on the fly. For Linux, we are still limited to the set of supported
distributions, but we can build for slightly different variations or versions
if needed. If you are running a Linux system other than a supported
distribution, you can also compile and install ZFS yourself, provided it's
version `0.8.1`, and Titan will use that instead of trying to install its own.

If the installation is taking a while, and you see a ``zfs-builder``
container in ``docker ps`` output, then it's off building a custom version
of ZFS. If you are running a supported operating system, then reach out to the
community to see if new pre-built binaries need to be created.

MacOS and Windows
-----------------
MacOS and Windows operate in a similar fashion, with an embedded
`HyperKit <https://github.com/moby/hyperkit>`_ VM running Linux behind the
scenes. This VM runs `LinuxKit <https://github.com/linuxkit/linuxkit>`_.
Titan supports the latest Docker Desktop releases. If you are running a very
old distribution (for example, using the old ``boot2docker`` framework on
Windows), you mileage may vary. You should update to the latest version of
Docker Desktop prior to using Titan.

.. note::

   If a brand new Docker Desktop release comes out that ships with a new
   LinuxKit kernel, it may take some time for the community to update the
   pre-built packages for the newest versions. If you encounter titan
   trying to build packags for a new ZFS kernel, head over to the
   `zfs-releases <https://github.com/titan-data/zfs-releases>`_ repository
   and open an issue (or PR) with the new version and ``uname -a`` output.

.. note::

   Titan does not work with the new
   `Docker for WSL <https://docs.docker.com/docker-for-windows/wsl-tech-preview/>`_
   due to limitations in Docker that prevent local volumes from working
   properly.

Linux
-----

The situation with Linux is quite different. With Linux, there's no standard
Docker VM being provided by Docker Desktop. Instead, we're running on whatever
host VM you have. Because each distribution requires distro-specific mechanisms
to download and install the right kernel files required to build ZFS, we have
a much more limited support matrix. In addition, distros will often build
specialized versions for different clouds (e.g. AWS, Azure, GCP), requiring
specific builds for each of them. You're much more likely to encounter an
unsupported platform when running Titan on Linux, but the community tries to
provide pre-built binaries for:

* Ubuntu Bionic (18.04) and later
* CentOS 7 and later
* LinuxKit as used by Docker Desktop

If you are running one of these flavors and not finding pre-built binaries
available, it's likely just a matter of updating the
`zfs-releases <https://github.com/titan-data/zfs-releases>`_ repository with
the latest build information. If you are trying to use a different distribution
alltogether, or are trying a new major version for the first time in the
community, you may need to update the
`zfs-builder <https://github.com/titan-data/zfs-builder>`_ repository such that
it knows how to build the new variant.

.. note::

   If you are using an unsupported Linux version, you can always
   `install ZFS <https://github.com/zfsonlinux/zfs/wiki/Building-ZFS>`_
   yourself. Titan will use any installed ZFS, version 0.8.1 or later, and
   won't attempt to install its own modules.
