.. _local_run:

Creating a new Repository
=========================

A titan repository combines a docker container and its configuration with data
that backs the volumes within that container. So every repository should
have a running docker container associated with it. For more information on
how the docker configuration is used, see the :ref:`local_docker` section.

To run a repository, you simply take the arguments you'd normally supply to
``docker run`` and pass that to ``titan run`` instead::

    $ titan run -- --name mymongo -d mongo:latest
    Creating repository mongo
    Creating docker volume mongo/v0 with path /data/configdb
    Creating docker volume mongo/v1 with path /data/db
    Running controlled container mongo
    $ docker ps | grep mongo
    755f23d7bbc9        mongo               "docker-entrypoint.s…"   6 seconds ago       Up 5 seconds        27017/tcp                mongo
    $ docker exec -it mymongo mongo --quiet
    > db.names.insert({ firstName: "Dorothy", lastName: "Vaughan" })
    WriteResult({ "nInserted" : 1 })
    > db.names.find()
    { "_id" : ObjectId("5d8d011a81973f4255f6bf25"), "firstName" : "Dorothy", "lastName" : "Vaughan" }

There are a couple things to note here:

* Titan requires a ``--name`` parameter, which it uses to name the repo by
  the same value. It doesn't currently support auto-generating names.
* Titan requires a ``-d`` parameter to run in the background as a daemon.
  We don't currently see a workflow that would benefit from running
  titan-backed containers in the foreground, but if you have such a need
  please join the community to share your use case and help develop a
  solution.
* Titan will use any docker image published to a register. In this case,
  we pull down and run ``mongo:latest``. The only requirement is that the
  containers must have one or more ``VOLUME`` declarations that indicate
  where persistent data is stored.
* In this example, we did not map any ports because we're using the
  MongoDB shell from within the container. Chances are in normal operation
  you'd want to use something like ``-p 27017:27017`` to map the mongo
  port to the expected port on your laptop.
