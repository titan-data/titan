.. _remote_provider_ssh:

SSH Provider
============

The SSH provider enables commits to be stored on any server where the user
has remote access over SSH. The URI syntax is::

    ssh://user[:password]@host/path

The ``path`` is interpreted as an absolute path unless it starts with ``~``.
The SSH provider uses rsync to copy files to subdirectories within the path,
with metadata being stored in a ``metadata.json`` file. This means that pushes
are always full sends, as titan is sending data to a newly created directory.
Pulls, on the other hand, may not need to transfer all data depending on what
state exists locally.

The system must have ``sudo`` installed and the user must have ``sudo``
privileges for running rsync. This enables file ownership and permissions to be
set properly.

If ``password`` is not specified, then the user will be prompted for a password
at the time they do the push or pull operation. Future enhancements will
include the ability to specify a SSH key file instead of using passwords.

Like the S3 provider, the SSH provider has inherent scalability limitations. For
example, finding the latest commit requires listing all commits in the path,
reading the metadata file for each, and comparing the result.  It should only be
used for storing relatively small numbers of commits. Improving this will
require a new provider that includes a robust metadata layer on top of the base
SSH functionality.
