.. _local_commit:

Committing Changes
==================

Once you have a repository running as outlined in the
:ref:`local_run` section, you can start to commit changes. Picking up where
the previous example left off, we can create some data and then commit
that state::

    $ docker exec -it mymongo mongo --quiet
    > db.names.insert({ firstName: "Katherine", lastName: "Goble" })
    WriteResult({ "nInserted" : 1 })
    > db.names.insert({ firstName: "Mary", lastName: "Jackson" })
    WriteResult({ "nInserted" : 1 })
    > db.names.find()
    { "_id" : ObjectId("5d8d011a81973f4255f6bf25"), "firstName" : "Dorothy", "lastName" : "Vaughan" }
    { "_id" : ObjectId("5d8d035b4958f33cf90b4d83"), "firstName" : "Katherine", "lastName" : "Goble" }
    { "_id" : ObjectId("5d8d036e4958f33cf90b4d84"), "firstName" : "Mary", "lastName" : "Jackson" }
    >
    $ titan commit -m "hidden figures" mymongo
    Commit 503d7863-14b7-4c39-b609-778fb976ba6a

The ``commit`` command took a snapshot of the on-disk state for the mongodb
container ``mymongo`` and stored that in the repository. It should now be
visible in the log::

    $ titan log mymongo
    commit 503d7863-14b7-4c39-b609-778fb976ba6a
    User: Anne Smith
    Email: anne.smith@gmail.com
    Date: 2019-09-26T18:34:48Z

    hidden figures

From here, we can add additional state and create a second commit::

    $ docker exec -it mymongo mongo --quiet
    > db.names.insert([{ firstName: "Grace", lastName: "Hopper" }, { firstName: "Ada", lastName: "Lovelace" }])
    BulkWriteResult({
    	"writeErrors" : [ ],
    	"writeConcernErrors" : [ ],
    	"nInserted" : 2,
    	"nUpserted" : 0,
    	"nMatched" : 0,
    	"nModified" : 0,
    	"nRemoved" : 0,
    	"upserted" : [ ]
    })
    > db.names.find()
    { "_id" : ObjectId("5d8d011a81973f4255f6bf25"), "firstName" : "Dorothy", "lastName" : "Vaughan" }
    { "_id" : ObjectId("5d8d035b4958f33cf90b4d83"), "firstName" : "Katherine", "lastName" : "Goble" }
    { "_id" : ObjectId("5d8d036e4958f33cf90b4d84"), "firstName" : "Mary", "lastName" : "Jackson" }
    { "_id" : ObjectId("5d8d05a643a059308e3dfae7"), "firstName" : "Grace", "lastName" : "Hopper" }
    { "_id" : ObjectId("5d8d05a643a059308e3dfae8"), "firstName" : "Ada", "lastName" : "Lovelace" }
    >
    $ titan commit -m "more great scientists" mymongo
    Commit 15ce0a6e-f15e-47a4-a65a-305fcb9efa5c

Once we have this committed state, we can easily go back to a previous state
by running ``titan checkout``::

    $ titan checkout -c 503d7863-14b7-4c39-b609-778fb976ba6a mymongo
    Stopping container mymongo
    Checkout 503d7863-14b7-4c39-b609-778fb976ba6a
    Starting container mymongo
    503d7863-14b7-4c39-b609-778fb976ba6a checked out
    $ docker exec -it mymongo mongo --quiet
    > db.names.find()
    { "_id" : ObjectId("5d8d011a81973f4255f6bf25"), "firstName" : "Dorothy", "lastName" : "Vaughan" }
    { "_id" : ObjectId("5d8d035b4958f33cf90b4d83"), "firstName" : "Katherine", "lastName" : "Goble" }
    { "_id" : ObjectId("5d8d036e4958f33cf90b4d84"), "firstName" : "Mary", "lastName" : "Jackson" }
    >

Here you can see that we stopped the container, swapped out the data, and
started it again. And with that, we're back to the original commit we created.

.. warning::

   The titan infrastructure has not currently been built for scale, and while it
   should work fine for dozens of commits, creating hundreds or thousands of
   commits or repositories may have adverse effects on the system. This will be
   addressed in a future release.

For information on more additional local workflows, see the
:ref:`local` section.
