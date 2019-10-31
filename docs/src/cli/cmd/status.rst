.. _cli_cmd_status:

titan status
============

Display the current status for a repository.

* Status - The current status of a repository (detached, running, stopped, etc...).
* Uncompressed Size - The Uncompressed Size of the repository on disk.
* Compressed Size - The Compressed Size of the repository on disk.
* Last Commit - The GUID (globally unique identifier) for the last commit.
* Source Commit - The GUID of the commit from which the current state is derived.
  This could be the last commit (if a commit has been created since the last
  checkout), or the commit from which the current state was checked out
  from.

For each volume, the following information is displayed:

* Volume - The path of the volume on the disk
* Uncompressed Size - The Uncompressed Size of the volume on disk.
* Compressed Size - The Compressed Size of the volume on disk.

All sizes reflect the size of the currently active volume state. As data is added, or if a different commit is checked out, these values will change.

Syntax
------

::

    titan status <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.


Example
-------

::

    $ titan status hello-world
                Status:  running
     Uncompressed Size:  526.5 KiB
       Compressed Size:  254 KiB
           Last Commit:  12c6da4d57004d3497afca4fb914ed58
         Source Commit:

    Volume                          Uncompressed  Compressed
    /var/lib/postgresql/data        31.7 MiB      6.9 MiB
