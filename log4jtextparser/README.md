Java application that can parse log4 logfile with multiline messages and put them into MongoDB using appender.

Currently, the application is limited to specific log4j layout. But it is easy to adopt this example application to use your own log4j layout.
And one last thing, MongoDB credential are coded directly in java file, so, you may want to change it someday.