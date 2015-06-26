/*
 * Copyright (C) 2009 Peter Monks (pmonks@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.focusit.log4jmongo.appender;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.spi.ErrorCode;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Log4J Appender that writes log events into a MongoDB document oriented database. Log events are
 * fully parsed and stored as structured records in MongoDB (this appender does not require, nor use
 * a Log4J layout).
 *
 * The appender does <u>not</u> create any indexes on the data that's stored - it is assumed that if
 * query performance is required, those would be created externally (e.g., in the MongoDB shell or
 * other external application).
 *
 * @author Peter Monks (pmonks@gmail.com)
 * @see <a href="http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Appender.html">Log4J
 *      Appender Interface</a>
 * @see <a href="http://www.mongodb.org/">MongoDB</a>
 */
public class SimpleMongoDbAppender extends AbstractBsonAppender {
    private final static String DEFAULT_MONGO_DB_HOSTNAME = "localhost";
    private final static String DEFAULT_MONGO_DB_PORT = "27017";
    private final static String DEFAULT_MONGO_DB_DATABASE_NAME = "log4mongo";
    private final static String DEFAULT_MONGO_DB_COLLECTION_NAME = "logevents";
    private WriteConcern concern;

    private String hostname = DEFAULT_MONGO_DB_HOSTNAME;
    private String port = DEFAULT_MONGO_DB_PORT;
    private String databaseName = DEFAULT_MONGO_DB_DATABASE_NAME;
    private String collectionName = DEFAULT_MONGO_DB_COLLECTION_NAME;
    private String userName = null;
    private String sourceDb = null;
    private String password = null;
    private String writeConcern = null;
	protected MongoClient mongo = null;
	protected MongoCollection<Document> collection = null;
    private String tag=null;

    protected boolean initialized = false;

    /**
     * @see org.apache.log4j.AppenderSkeleton#activateOptions()
     */
    @Override
    public void activateOptions() {
        try {
            // Close previous connections if reactivating
            if (mongo != null) {
                close();
            }

            initialize();

        } catch (final Exception e) {
            errorHandler.error("Unexpected exception while initialising MongoDbAppender.", e,
                    ErrorCode.GENERIC_FAILURE);
        }
    }

    /**
     * @param bson
     *            The BSON object to insert into a MongoDB database collection.
     */
    @Override
    public void append(final Document bson) {
        if (initialized && bson != null) {
            try {
            	if(tag!=null){
            		bson.put("tag", tag);
            	}
                getCollection().insertOne(bson);
            } catch (final MongoException e) {
                errorHandler.error("Failed to insert document to MongoDB", e,
                        ErrorCode.WRITE_FAILURE);
            }
        }
    }

	/**
	 * @see org.apache.log4j.Appender#close()
	 */
	@Override
	public void close() {
		if (mongo != null) {
			collection = null;
			mongo.close();
		}
	}

    /**
     * @return The collection used within the database in the MongoDB server <i>(will not be null,
     *         empty or blank)</i>.
     */
    public String getCollectionName() {
        return collectionName;
    }

    public WriteConcern getConcern() {
    	if (concern == null) {
    		concern = getCollection().getWriteConcern();
    	}
		return concern;
	}

    /**
     * @return The database used in the MongoDB server <i>(will not be null, empty or blank)</i>.
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @return The hostname of the MongoDB server <i>(will not be null, empty or blank)</i>.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return The port of the MongoDB server <i>(will be > 0)</i>.
     */
    public String getPort() {
        return port;
    }

    public String getSourceDb() {
		return sourceDb;
	}

    public String getTag() {
		return tag;
	}

    /**
     * @return The userName used to authenticate with MongoDB <i>(may be null)</i>.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the writeConcern setting for Mongo.
     */
    public String getWriteConcern() {
		return writeConcern;
	}

    /**
     * Returns true if appender was successfully initialized. If this method returns false, the
     * appender should not attempt to log events.
     *
     * @return true if appender was successfully initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    @Override
	public boolean requiresLayout() {
        return (false);
    }

    /**
     * Note: this method is primarily intended for use by the unit tests.
     *
     * @param collection
     *            The MongoDB collection to use when logging events.
     */
    public void setCollection(final MongoCollection<Document> collection) {
        assert collection != null : "collection must not be null";

        this.collection = collection;
    }

    /**
     * @param collectionName
     *            The collection used within the database in the MongoDB server <i>(must not be
     *            null, empty or blank)</i>.
     */
    public void setCollectionName(final String collectionName) {
        assert collectionName != null : "collection must not be null";
        assert collectionName.trim().length() > 0 : "collection must not be empty or blank";

        this.collectionName = collectionName;
    }

    /**
     * @param databaseName
     *            The database to use in the MongoDB server <i>(must not be null, empty or
     *            blank)</i>.
     */
    public void setDatabaseName(final String databaseName) {
        assert databaseName != null : "database must not be null";
        assert databaseName.trim().length() > 0 : "database must not be empty or blank";

        this.databaseName = databaseName;
    }

    /**
     * @param hostname
     *            The MongoDB hostname to set <i>(must not be null, empty or blank)</i>.
     */
    public void setHostname(final String hostname) {
        assert hostname != null : "hostname must not be null";
        assert hostname.trim().length() > 0 : "hostname must not be empty or blank";

        this.hostname = hostname;
    }

    /**
     * @param password
     *            The password to use when authenticating with MongoDB <i>(may be null)</i>.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @param port
     *            The port to set <i>(must not be null, empty or blank)</i>.
     */
    public void setPort(final String port) {
        assert port != null : "port must not be null";
        assert port.trim().length() > 0 : "port must not be empty or blank";

        this.port = port;
    }

    public void setSourceDb(final String sourceDb) {
		this.sourceDb = sourceDb;
	}

    public void setTag(final String tag) {
		this.tag = tag;
	}

    /**
     * @param userName
     *            The userName to use when authenticating with MongoDB <i>(may be null)</i>.
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    /**
     * @param writeConcern
     * 				The WriteConcern setting for Mongo.<i>(may be null). If null, set to default of dbCollection's writeConcern.</i>
     */
    public void setWriteConcern(final String writeConcern) {
    	this.writeConcern = writeConcern;
		concern = WriteConcern.valueOf(writeConcern);
	}

    private List<Integer> getPortNums(final String[] ports) {
        final List<Integer> portNums = new ArrayList<Integer>();

        for (final String port : ports) {
            try {
                final Integer portNum = Integer.valueOf(port.trim());
                if (portNum < 0) {
                    errorHandler.error(
                            "MongoDB appender port property can't contain a negative integer",
                            null, ErrorCode.ADDRESS_PARSE_FAILURE);
                } else {
                    portNums.add(portNum);
                }
            } catch (final NumberFormatException e) {
                errorHandler.error(
                        "MongoDB appender can't parse a port property value into an integer", e,
                        ErrorCode.ADDRESS_PARSE_FAILURE);
            }

        }

        return portNums;
    }

    /**
     * Returns a List of ServerAddress objects for each host specified in the hostname property.
     * Returns an empty list if configuration is detected to be invalid, e.g.:
     * <ul>
     * <li>Port property doesn't contain either one port or one port per host</li>
     * <li>After parsing port property to integers, there isn't either one port or one port per host
     * </li>
     * </ul>
     *
     * @param hostname
     *            Blank space delimited hostnames
     * @param port
     *            Blank space delimited ports. Must specify one port for all hosts or a port per
     *            host.
     * @return List of ServerAddresses to connect to
     */
    private List<ServerAddress> getServerAddresses(final String hostname, final String port) {
        final List<ServerAddress> addresses = new ArrayList<ServerAddress>();

        final String[] hosts = hostname.split(" ");
        final String[] ports = port.split(" ");

        if (ports.length != 1 && ports.length != hosts.length) {
            errorHandler.error(
                    "MongoDB appender port property must contain one port or a port per host",
                    null, ErrorCode.ADDRESS_PARSE_FAILURE);
        } else {
            final List<Integer> portNums = getPortNums(ports);
            // Validate number of ports again after parsing
            if (portNums.size() != 1 && portNums.size() != hosts.length) {
                errorHandler
                        .error("MongoDB appender port property must contain one port or a valid port per host",
                                null, ErrorCode.ADDRESS_PARSE_FAILURE);
            } else {
                final boolean onePort = (portNums.size() == 1);

                int i = 0;
                for (final String host : hosts) {
                    final int portNum = (onePort) ? portNums.get(0) : portNums.get(i);
                    addresses.add(new ServerAddress(host.trim(), portNum));
                    i++;
                }
            }
        }
        return addresses;
    }

	/**
     *
     * @return The MongoDB collection to which events are logged.
     */
    protected MongoCollection<Document> getCollection() {
        return collection;
    }

	/*
     * This method could be overridden to provide the DB instance from an existing connection.
     */
    protected MongoDatabase getDatabase(final MongoClient mongo, final String databaseName) {
        return mongo.getDatabase(databaseName);
    }

	/*
     * This method could be overridden to provide the Mongo instance from an existing connection.
     */
    protected MongoClient getMongo(final List<ServerAddress> addresses) {

    	List<MongoCredential> credential = null;

    	if(userName!=null && password!=null && sourceDb !=null && userName.trim().length() > 0 && password.trim().length() > 0 && sourceDb.trim().length() > 0){
    		credential = new ArrayList<MongoCredential>();
    		credential.add(MongoCredential.createCredential(userName, sourceDb, password.toCharArray()));
    	}

    	if(credential!=null) {
    		return new MongoClient(addresses, credential);
    	}
        return new MongoClient(addresses);
    }

	protected void initialize(){
        final List<ServerAddress> addresses = getServerAddresses(hostname, port);
        mongo = getMongo(addresses);
        mongo.setWriteConcern(WriteConcern.valueOf(getWriteConcern()));

        final MongoDatabase database = getDatabase(mongo, databaseName);

        if (userName != null && userName.trim().length() > 0) {
            // Allow password to be GCed
            password = null;
        }

        setCollection(database.getCollection(collectionName));
        initialized = true;
    }

}
