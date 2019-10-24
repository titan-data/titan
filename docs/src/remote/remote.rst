.. _remote:

Remote Repositories
===================

While managing data locally on your laptop is all well and good, part of the
power of source code management is the ability to share that data with
others. Much like git, Titan has the notion of `remote repositories` that
act as an endpoint for push and pull.

There are a few important general things to be aware of:

* Titan commits do not have a strict dependency on the previous commit from
  which it was created. Because they are much larger, we allow them to be
  pushed and pulled independently. For this reason, :ref:`cli_cmd_clone` and
  :ref:`cli_cmd_pull` will not pull down `all commits`, only the one specified
  by the user.
* Titan does not support the notion of merging. While concepts like tagging
  and branching will be added over time, generically merging data at the
  on-disk level is not possible.
* Different remote providers have different performance characteristics,
  including whether they support incremental transfers. Some will
  always to a full data transfer, while others have a means to identify
  only changed blocks. Titan is designed to work with small
  datasets (<10GB), using it for anything remotely large may have adverse
  effects on the system.

.. warning::

   Titan currently ships with two very basic providers, the :ref:`remote_provider_s3`
   and the :ref:`remote_provider_ssh`. These are only introductory providers, designed
   to have zero dependencies on external software. But as such, they
   will face challenges across security, performance, and robustness when
   operated at scale in an enterprise setting. As Titan matures, we will be
   working with the community and partners to help develop remote providers
   with more robust capabilities.

.. toctree::
   :maxdepth: 1
   :caption: Working with Remotes

   addremove
   pushpull
   clone

.. toctree::
   :maxdepth: 1
   :caption: Remote Providers

   provider/s3
   provider/s3web
   provider/ssh
