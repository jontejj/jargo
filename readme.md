# Jargo
A tool to ease the handling of program arguments/options  
[![Build Status](https://travis-ci.org/Softhouse/jargo.png)](https://travis-ci.org/Softhouse/jargo) 
[Jenkins](https://jontejj.ci.cloudbees.com/job/jargo/)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.softhouse/se.softhouse.jargo/badge.svg?style=flat)](http://search.maven.org/#search|ga|1|a%3Ajargo)

# Most basic usage
```java
import static se.softhouse.jargo.Arguments.*;
...
String[] args = {"--enable-logging", "--listen-port", "8090", "Hello"};

Argument<?> helpArgument = helpArgument("-h", "--help"); //Will throw when -h is encountered
Argument<Boolean> enableLogging = optionArgument("-l", "--enable-logging")
									.description("Output debug information to standard out").build();
Argument<String> greetingPhrase = stringArgument()
									.description("A greeting phrase to greet new connections with").build();
Argument<List<Integer>> ports = integerArgument("-p", "--listen-port")
									.defaultValue(8080)
									.description("The port clients should connect to.")
									.metaDescription("<port>")
									.limitTo(Range.closed(0, 65536))
									.repeated().build();

try
{
	ParsedArguments arguments = CommandLineParser.withArguments(greetingPhrase, enableLogging, ports)
													.andArguments(helpArgument)
													.parse(args);
													
	System.out.println("Logging enabled: " + arguments.get(enableLogging));
	System.out.println("Ports: " + arguments.get(ports));
	System.out.println("Greeting visitors with: " + arguments.get(greetingPhrase));
}
catch(ArgumentException exception)
{
	System.out.println(exception.getMessageAndUsage());
	System.exit(1);
}
```
For more examples see the [Javadoc](http://softhouse.github.com/jargo/javadoc/jargo/)

# Dependency
#### Jargo
     <dependency>
       <groupId>se.softhouse</groupId>
       <artifactId>jargo</artifactId>
       <version>0.1.1</version>
     </dependency>
  
#### Common-test (optional) [Javadoc](http://softhouse.github.com/jargo/javadoc/common-test/)
     <dependency>
      <groupId>se.softhouse</groupId>
      <artifactId>common-test</artifactId>
      <version>0.1.1</version>
  </dependency>

# Bugs/Questions
[Issues](https://github.com/Softhouse/jargo/issues)  
[QA forum](https://groups.google.com/forum/?fromgroups=&hl=en#!forum/jargo)

# Rationale
 for writing yet another argument parsing library (see [Is there a good command line argument parser for Java?](http://stackoverflow.com/a/7829772) for others)

1. Because type-safety, immutability and readability matters

2. Compared to annotation based solutions (like [JCommander](http://www.jcommander.org)) jargo can be updated at runtime to support more arguments

3. The generics on [Argument](http://softhouse.github.com/jargo/javadoc/jargo/se/softhouse/jargo/Argument.html) gives you compile-time errors when switching types

    In JCommander:  
    ```java
    @Parameter(names = "-file", converter = FileConverter.class)  
    File file; //Oops, changing this to int would not work very well with FileConverter
    ```

    In jargo:  
    ```java
    Arguments.fileArgument("-file").parse("-file", "filename.txt").createNewFile();
    ```

    if fileArgument would change to being integerArgument, trying to call createNewFile() would generate a compile-time error

4. Because JCommander doesn't support repeated arguments other than List&lt;String&gt;
    ```java
    String[] args = {"--numbers", "5", "6", "--numbers", "3", "4"};  
    Argument<List<List<Integer>>> numbers = Arguments.integerArgument("--numbers").arity(2).repeated().build();
    ```

5. Because I love [Guava](https://code.google.com/p/guava-libraries/) and wanted an argument parsing
    library well integrated with it (more to come in this department)

6. Reflection makes it hard to analyze references to classes/methods and it
    often requires a granted suppressAccessChecks from the SecurityManager, this may not be wanted. No reflection is used in jargo.
