# MongoDB configuration
## mongod.conf

### wiredtiger engine
```yaml
storage:
    dbPath: "/var/lib/mongodb"
    directoryPerDB: true
    journal:
        enabled: true
    engine: "wiredTiger"
    wiredTiger:
        engineConfig: 
\#            cacheSizeGB: 8 
            journalCompressor: snappy
        collectionConfig: 
            blockCompressor: snappy
        indexConfig:
            prefixCompression: true
```

### http options (for debug)
```yaml
net:
   http:
      enabled: true
      JSONPEnabled: true
      RESTInterfaceEnabled: true

```
## database options
You can create a db with desired engine
```
db.createCollection( "log4j", { storageEngine: { wiredTiger: { configString: 'block_compressor=snappy' }}})
```

then, you, probably, need to define indexes

```bash
#!/bin/sh

COLLECTION=log
DB=log4j

echo "db.${COLLECTION}.createIndex({\"timestamp\":1});"|mongo $DB
echo "db.${COLLECTION}.createIndex({\"timestamp\":-1});"|mongo $DB

echo "db.${COLLECTION}.createIndex({\"level\":-1});"|mongo $DB
echo "db.${COLLECTION}.createIndex({\"level\":1});"|mongo $DB
echo "db.${COLLECTION}.createIndex({\"level\":\"hashed\"});"|mongo $DB

echo "db.${COLLECTION}.createIndex({\"method\":\"hashed\"});"|mongo $DB
echo "db.${COLLECTION}.createIndex({\"method\":\"text\"});"|mongo $DB

echo "db.${COLLECTION}lection.createIndex({ \"$**\": \"text\" },{ name: \"TextIndex\" });"|mongo $DB
```

## collection options
You can capped collection used to hold event data 

```
db.createCollection("log", { capped : true, size : 5242880, max : 5000 } ) 
```

#Useful queries to get logs
Get warn errors and fatal events
```
db.log.find({$or:[{"level":"WARN"},{"level": "ERROR"}, {"level": "FATAL"}]}, {"_id":1, "tag": 1, "timestamp": 1, "level": 1, "thread": 1, "message": 1, "loggerName.fullyQualifiedClassName": 1, "throwables": 1}).sort({"timestamp":-1})
```

Search thought log
```
db.log.find({ $and: [ { "level": "INFO" }, { "message": { $regex: '^\QRoot WebApplicationContext: initialization completed in\E.*' } } ] }, {"_id":1, "tag": 1, "timestamp": 1, "level": 1, "thread": 1, "message": 1, "loggerName.fullyQualifiedClassName": 1, "throwables": 1}).sort({"timestamp":-1})
```

#Run queries from command line
```
echo "db.log.find()" | mongo log4j
```

#Getting notifications from mongo

```python
from pymongo import Connection
import time

db = Connection().log4j
coll = db.log
cursor = coll.find(tailable=True)
while cursor.alive:
    try:
        doc = cursor.next()
        print doc
    except StopIteration:
        time.sleep(1)
```

```ruby
db   = Mongo::Connection.new().db('log4j')
coll = db.collection('log')
cursor = Mongo::Cursor.new(coll, :tailable => true)
loop do
  if doc = cursor.next_document
    puts doc
  else
    sleep 1
  end
end
```