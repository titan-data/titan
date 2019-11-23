.. _local_run:

Creating a new Repository
=========================

A titan repository combines a docker container and its configuration with data
that backs the volumes within that container. So every repository should
have a running docker container associated with it. For more information on
how the docker configuration is used, see the :ref:`local_docker` section.

To run a repository, you simply take the arguments you'd normally supply to
``docker run`` and pass that to ``titan run`` instead::

    $ titan run mongo
    Creating repository mongo
    Creating docker volume mongo/v0 with path /data/configdb
    Creating docker volume mongo/v1 with path /data/db
    Running controlled container mongo
    $ docker ps | grep mongo
    755f23d7bbc9        mongo               "docker-entrypoint.s…"   6 seconds ago       Up 5 seconds         0.0.0.0:27017->27017/tcp                mongo
    $ docker exec -it mongo mongo --quiet
    > db.names.insert({ firstName: "Dorothy", lastName: "Vaughan" })
    WriteResult({ "nInserted" : 1 })
    > db.names.find()
    { "_id" : ObjectId("5d8d011a81973f4255f6bf25"), "firstName" : "Dorothy", "lastName" : "Vaughan" }

There are a couple things to note here:

* Titan will use any docker image published to a register. In this case,
  we pull down and run ``mongo:latest``. The only requirement is that the
  containers must have one or more ``VOLUME`` declarations that indicate
  where persistent data is stored.
* In this example, titan mapped the default exposed port from the docker
  image to localhost. This is the same thing as manually adding ``-p 27017:27017``
  to map the mongo port to the expected port on your laptop.
