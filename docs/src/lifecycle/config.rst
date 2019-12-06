:: _lifecycle_config

Repository Configuration
========================

Whenever a new repository is created, Titan will store metadata about the
associated container image that is then stored with each commit and used to
instantiate containers in the appropriate runtime context. Each repository
and commit stores the following information taken from the original container
image:

* Image Name
* Image Tag
* Image Digest
* Environment Variables
* Volume Mappings
* Exposed Ports

Image Configuration
-------------------

Whenever Titan creates a new container, Titan will attempt to use the identical
image that was used to create the data. For images that have been pushed to
a registry, this is accomplished through the image digest, which ensures that
an exact match is used. There are cases where the digest doesn't exist, as
when using images built locally that have not been pushed to a registry,
or where the digest is unavailable, such as when the image came from a private
registry that is no longer available. In these cases, the image name and tag
is used instead. For example, if the ``postgres:11`` image is used, Titan
will prefer the digest of that image, as new images (corresponding to
Postgres minor releases) may be pushed under that tag. But in the event that
digest can't be found, Titan will attempt to use whatever image corresponds to
``postgres::11`` at that point in time. Radically changing the image
configuration (most notably volume mappings) can have unintended consequences
and cause the repository to fail to be created.

.. attention::

  There is currently no way to override the image attributes to change
  which image to use for a repository. This capability will be added in a
  future release.

.. attention::

  Titan does not currently perform rigorous compatibility checks when pushing
  commits to a remote. It is possible to push radically different commits
  (different images, different volume mappings) that can cause significant
  issues when trying to switch between data states in a repository. Be careful
  that you are only pushing and pull compatible commits.

Port Forwarding
---------------

By default, Titan will forward any exposed ports on the container to the local
system. For example, running a PostgreSQL image will by default make the
container available at ``localhost:5432``. To disable this behavior, use the
``-P`` option when cloning or running a repository. Custom port mappings can
be creating using additional context-specific configuration options, described
below.

Additional Configuration
------------------------

Additional context-specific configuration can be specified when cloning or
running a repository, but that configuration will not be persisted with the
commits. For example, you can choose to forward ports differently (or not
at all) on your workstation, run the containers on alternate docker networks,
or use a different storage class for volumes in Kubernetes. This configuration
may not translate across runtime environments (for example, a Docker network
may not exist on a different system, or may not make sense at all in a
Kubernetes environment), so you will have to specify it each time a repository
is run or cloned. Once configured, this configuration will continue to be used
for subsequent checkouts or commits.
