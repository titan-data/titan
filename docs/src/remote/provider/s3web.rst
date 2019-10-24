.. _remote_provider_s3web:

S3 Web Provider
===============

The S3 web provider provides read-only access to data pushed to a S3 remote,
using the HTTP terface. It is designed to make it easy to share public
data without needing AWS credentials of any kind.
The URI format is::

    s3web://<bucket-url>[:port]/<path>

The format of the data must match that pushed by the :ref:`remote_provider_s3`.
The bucket URL can be anything, including DNS aliases or cloud front
distributions sitting in front of the bucket. The only requirement is that
``HTTP GET`` is supported for objects beneath the path.

.. note::

   The S3 Web provider is read-only. It can only pull data that has been pushed by
   the :ref:`remote_provider_s3`.
