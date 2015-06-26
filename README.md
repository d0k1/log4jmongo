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
* Log4J 1.2+

# Maven usage

First of all add a repository to pom.xml
```
<repositories>
	<repository>
    	<id>jitpack.io</id>
    	<url>https://jitpack.io</url>
	</repository>
</repositories>

```

Then, add dependecy

```
<dependency>
    <groupId>com.github.d0k1</groupId>
    <artifactId>log4jmongo</artifactId>
    <version>1.0</version>
</dependency>
```

If you want you may see build logs here https://jitpack.io/com/github/d0k1/log4jmongo/1.0/build.log
 
# References
* [1] http://logging.apache.org/log4j/1.2/index.html
* [2] http://www.mongodb.org/
* [3] http://github.com/log4mongo/log4mongo-java
