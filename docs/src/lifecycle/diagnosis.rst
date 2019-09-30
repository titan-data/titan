.. _lifecycle_diagnosis:

Diagnosing Issues
=================

Titan strives to present error messages in an easy-to-consume, readable
fashion. But the reality is that we can't anticipate everything, and as a
relatively new community there are plenty of rough edges. If something
goes wrong and the error message doesn't help you, this section provides
some additional tools for you to try and self-diagnose the issue prior to
engaging with the `Community <https://titan-data.io/community>`_.

Docker Logs
-----------
The most important tool in the diagnosis tool belt is to look at the
docker logs, specifically ``docker logs titan-server``. This will show the
output of the server that is responding to requests from the CLI.
Example output::

    2019-09-30 16:18:27.223 [DefaultDispatcher-worker-4] INFO  ktor.application - 200 OK: GET - /v1/repositories/hello-world/operations/1dfadd4f-a376-4ab7-9f88-c7f4c1249634
    2019-09-30 16:18:28.530 [DefaultDispatcher-worker-6] DEBUG io.titandata.util.CommandExecutor - Success: zfs, list, -Hpo, io.titan-data:active, titan/repo/hello-world
    2019-09-30 16:18:28.535 [DefaultDispatcher-worker-6] DEBUG io.titandata.util.CommandExecutor - Success: zfs, list, -Ho, io.titan-data:metadata, titan/repo/hello-world/2db7b743-d643-4861-82db-71682e2ada26/v0
    2019-09-30 16:18:28.537 [DefaultDispatcher-worker-6] INFO  ktor.application - 200 OK: POST - /VolumeDriver.Get
    2019-09-30 16:18:28.548 [DefaultDispatcher-worker-6] INFO  i.t.storage.zfs.ZfsStorageProvider - unmount volume v0 in hello-world
    2019-09-30 16:18:28.561 [DefaultDispatcher-worker-6] DEBUG io.titandata.util.CommandExecutor - Success: zfs, list, -Hpo, io.titan-data:active, titan/repo/hello-world
    2019-09-30 16:18:28.640 [DefaultDispatcher-worker-6] DEBUG io.titandata.util.CommandExecutor - Success: umount, /var/lib/titan/mnt/hello-world/v0

In particular, every REST call from the client is logged, along with
every invocation of the zfs command. Understanding the semantics of the
behavior is beyond the scope of this section, but an important thing to note
is that any unexpected error should result in a stack trace that is quiet
visible in the log. If you start at the end of the log and work your way
backwards, if you find an exception that matches the behavior you're seeing,
then it may contain more information that helps diagnose the problem.

Docker State
------------
Sometimes the issue is not with titan, but with docker itself. For example,
if you try to start a container that has a port mapping conflict with another
container, it will fail to start. The CLI should display both the command
and the output from docker, but if you're still not sure what is going
on, you can try to run the command yourself and tweak some of the options
to see if you can get it to work.

Installation Issues
-------------------
When you're running ``titan install``, the server container may or may not
be running successfully. In these cases, you can use ``docker logs titan-launch``
to see what may be going on within the process. The lines denoted ``TITAN` are
designed to be more user-readable, while the other error messages may be
internal.

Local Client Errors
-------------------
It's possible that there are errors in the client that don't manifest as calls
to the container server. If this happens, and the problem is cannot be
discerned from the error message, you'll need to reach out to the community
for help. Once we have a better understanding of the problems that can
occur client-side, we will look into building local debugging facilities
within the command.
