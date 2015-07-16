package com.focusit.textparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.focusit.log4jmongo.appender.SimpleMongoDbAppender;

public class LogParser extends SimpleMongoDbAppender {
	private static String log4Pattern = "%r [%t] (%d{dd MMM yyyy HH:mm:ss,SSS}) %-5p %c{2} - %m%n";
	//                                   %r         %t            %d            %-5p       %c                       %m     %n     
	private static String eventRegex = "(^\\w+)\\s+\\[(.*)\\]\\s+\\((.*)\\)\\s+(\\w+)\\s+([A-Za-z0-9\\.]+)\\s+\\-\\s+(.*)(\\n|\\r\\n)?";
	private static String stacktraceRegex = "^((([A-Za-z0-9\\.]+\\: )|(Caused by:.*)|\\t).*(\\n|\\r\\n)?)";
	
	private Pattern eventPattern;
	private Pattern stacktracePattern;
	
	private long events = 0;
	
	public LogParser(){
		eventPattern = Pattern.compile(eventRegex, Pattern.MULTILINE);
		stacktracePattern = Pattern.compile(stacktraceRegex, Pattern.MULTILINE);
	}
	
	// This method should be overloaded in some way. At now this method use predefined log4j layout and it regex representation.
	private LoggingEvent parseMatcher(String event, Matcher m, String messageAdd, List<String> causes) throws ParseException{
		LoggingEvent e = null;
		String thread = m.group(2);
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+3"));
		long timestamp = sdf.parse(m.group(3)).getTime();
		String loggerFqn = m.group(5);
		Category cat = new EventCategory(loggerFqn);
		String level = m.group(4);
		Level prio = null;
		
		switch(level){
			case "FATAL": prio = Level.FATAL; break;
			case "ERROR":prio = Level.ERROR; break;
			case "WARN":prio = Level.WARN; break;
			case "INFO":prio = Level.INFO; break;
			case "DEBUG":prio = Level.DEBUG; break;
			case "TRACE":prio = Level.TRACE; break;
		}
		
		String message = m.group(6)+messageAdd;
		
		e = new LoggingEvent(loggerFqn, cat, timestamp, prio, message, thread, new ThrowableInformation(causes.toArray(new String[causes.size()])), (String)null, (LocationInfo)null, null);
		events++;
		System.out.println("Parsed "+events+" event(s). Last event on "+m.group(3));
		return e;
	}
	
	public LoggingEvent parseNextEvent(PushBackBufferedReader is) throws IOException, ParseException {
		String line;
		Matcher eventMathcer = null;
		String event = null;
		
		// First of all reading lines until find first event
		while ((line = is.readLine()) != null) {
			Matcher epm = eventPattern.matcher(line);
			if(epm.matches())
			{
				eventMathcer = epm;
				event = line;
				// marking this position to reset stream to this position
				//is.mark(0);
				break;
			}
		}
		
		// no lines left in file
		if(line==null)
		{
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		ArrayList<String> stack = new ArrayList<>();
		
		// detecting next line content: is it a multiline message, or a stacktrace element or may be it is next event 
		line = is.readLine();
		if(line==null){
			return parseMatcher(event, eventMathcer, builder.toString(), stack);
		}
		Matcher epm = eventPattern.matcher(line);

		if(epm.matches()){
			// if on next line comes event reset reader, and return parsed event
			is.pushBack();
			return parseMatcher(event, eventMathcer, builder.toString(), stack);
		} else {
			// definently there is no new event on next line. Make a test if it is a stacktrace element
			Matcher spm = stacktracePattern.matcher(line);
			if(!spm.matches()){
				// ok if it is not a stacktrace element, so it must be multiline message
				builder.append(line);
				
				is.mark(0);
				// read rest of lines until get a stacktrace element 
				while ((line = is.readLine()) != null) {
					epm = eventPattern.matcher(line);
					spm = stacktracePattern.matcher(line);
					if(epm.matches()){
						// new event - reset position and return parsed event
						is.pushBack();
						return parseMatcher(event, eventMathcer, builder.toString(), stack);
					} else if(spm.matches()){
						// if next line is a stacktrace element - must save it in special array for stacks
						stack.add(line);
					} else {
						builder.append(line);
					}
					is.mark(0);
				}
			} else {
				is.mark(0);
				stack.add(line);
				while ((line = is.readLine()) != null) {
					epm = eventPattern.matcher(line);
					spm = stacktracePattern.matcher(line);
					if(epm.matches()){
						// new event - reset position and return parsed event
						is.pushBack();
						return parseMatcher(event, eventMathcer, builder.toString(), stack);
					}
					if(spm.matches()){
						stack.add(line);
					}
				}				
			}
		}
		
		return null;
	}
	
	/**
	 * Special log4j category class to allow setting category' name from external string
	 * @author doki
	 *
	 */
	class EventCategory extends Category {

		public EventCategory(String name) {
			super(name);
		}		
	}
	
	/**
	 * Class to make java's buffered readed possibility to push back just read line.
	 * @author doki
	 *
	 */
	static class PushBackBufferedReader extends BufferedReader {

		private volatile String prevLine = null;
		private volatile boolean pushedBack = false;
		
		public PushBackBufferedReader(Reader in) {
			super(in);
		}

		@Override
		public String readLine() throws IOException {
			
			if(pushedBack){
				pushedBack = false;
				return prevLine;
			}
			
			prevLine = super.readLine(); 
			return prevLine;
		}
		
		public void pushBack(){
			pushedBack = true;
		}
	}
	
	public static void main(String[] args) throws ParseException{
		System.out.println("Starting parser");
		File lf = new File("sample.log");

		LogParser app = new LogParser();
		app.setCollectionName("log");
		app.setDatabaseName("log4mongo");
		app.setHostname("127.0.0.1");
		app.setWriteConcern("UNACKNOWLEDGED");
		app.initialize();
		
		try(PushBackBufferedReader br = new PushBackBufferedReader(new FileReader(lf))){
			
			LoggingEvent e = null;		
			e = app.parseNextEvent(br);
			while(e!=null){
				app.append(e);
				e = app.parseNextEvent(br);				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
