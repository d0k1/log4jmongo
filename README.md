Log4mongo
================
[Source code on GitHub](http://github.com/d0k1/log4mongo)

# Description
This library provides Log4J Appender [1] that write log events to the
MongoDB document oriented database [2].
This library based on http://github.com/log4mongo/log4mongo-java [3].

* MongoDbAppender - Stores a BSONed version of the Log4J LoggingEvent

# Authors
* Denis Kirpichenkov

# Original Library authors
* Peter Monks (pmonks@gmail.com)
* Robert Stewart (robert@wombatnation.com)

# Pre-requisites
* JDK 1.7+
* MongoDB Server v3.0+
* MongoDB Java Driver v3.0+
* Log4J 1.2+ (tested with 1.2.16 - note: tests won't work on earlier versions due to Log4J API changes)
* Privateer (used only in unit tests - a copy is in the lib dir, in case you can't get it
from the central Maven repo)

# Installation / Build / Configuration
If you downloaded a pre-built jar file, skip step 4.

1. Start local MongoDB servers running as a replica set. This is required for the replica set
part of the unit tests. The --smallfiles arg makes the unit tests run about twice as fast,
since databases are created and dropped several times, though it generally should not
be used in production. The --noprealloc and --nojournal options are also to speed up tests
and should not generally be used in production.
    
        $ sudo mkdir -p /data/r{0,1,2}
        $ sudo chown -r `whoami` /data
        $ mongod --replSet foo --smallfiles --noprealloc --nojournal --port 27017 --dbpath /data/r0
        $ mongod --replSet foo --smallfiles --noprealloc --nojournal --port 27018 --dbpath /data/r1
        $ mongod --replSet foo --smallfiles --noprealloc --nojournal --port 27019 --dbpath /data/r2
    
# References
* [1] http://logging.apache.org/log4j/1.2/index.html
* [2] http://www.mongodb.org/
* [3] http://github.com/mongodb/mongo-java-driver/downloads
* [4] http://github.com/maxaf/daybreak
* [5] http://bsonspec.org/#/specification
* [6] http://groups.google.com/group/mongodb-user/browse_thread/thread/e59cbc8c9ba30411/af061b4bdbce5287
