.. _cli_cmd_log:

titan log
=========

Display the history of commits for a repository. For each commit, the following
information is displayed:

* Commit ID - The GUID (globally unique identifier) for this commit.
* User - The name of the user that created the commit.
* Email - The email of the user that created the commit.
* Date - The timestamp of when the commit was created.
* Message - The message provided when the commit was created.

For more information on how author identity is determined, see the
:ref:`cli_cmd_commit` command.

Syntax
------

::

    titan log <repository>

Arguments
---------

repository
    *Required*. The name of the target repository.


Example
-------

::

    $ titan log myrepo
    commit 470ceb06-ebd3-486a-a4de-7f755df84309
    User: Eric Schrock
    Email: Eric.Schrock@delphix.com
    Date: 2019-09-26T13:30:52Z

    my first commit

    commit 0f53a6a4-90ff-4f8c-843a-a6cce36f4f4f
    User: Eric Schrock
    Email: Eric.Schrock@delphix.com
    Date: 2019-09-20T13:45:38Z

    demo data
