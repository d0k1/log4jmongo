package com.focusit.log4mongo.example;

import org.apache.log4j.Logger;

public class App {

	public static void main(String []args){
		Logger log = Logger.getLogger(App.class);
		log.fatal("FATAL: test");
		log.error("ERROR: test");
		log.warn("WARN: test");
		log.info("INFO: test");
		log.debug("DEBUG: test");
		log.trace("TRACE: test");
	}
}
