# Jargo
A tool to ease the handling of program arguments/options

# Most basic usage:
    String[] argv = {"--enable-logging", "--port", "8090", "Hello World!"};
    
    Argument<Boolean> enableLogging = Arguments.optionArgument("-l", "--enable-logging").
            description("Output debug information to standard out").build();
    
    Argument<Integer> port = Arguments.integerArgument("-p", "--port").defaultValue(8080).
            description("The port to start the server on.").build();
    
    Argument<String> greetingPhrase = Arguments.stringArgument().description("A greeting phrase to greet new connections with").build();
    
    ParsedArguments args = CommandLineParser.withArguments(greetingPhrase, enableLogging, port).parse(args);
    
    System.out.println("Logging enabled: " + args.get(enableLogging));
    System.out.println("Port: " + args.get(port));
    System.out.println("Greeting visitors with: " + args.get(greetingPhrase));

For more examples see javadoc on CommandLineParser and ArgumentBuilder.

Rationale for writing yet another argument parsing library (see [Is there a good command line argument parser for Java?](http://stackoverflow.com/a/7829772) for others):

1. Because type-safety, immutability and readability matters

2. Compared to annotation based solutions (like [JCommander](http://www.jcommander.org)) jargo can be updated at runtime to support more arguments

3. The generics on Argument gives you compile-time errors when switching types. For example:

    In JCommander:
    Parameter(names = "-file", converter = FileConverter.class) //Converted type from FileConverter checked at runtime
    File file; //Oops, changing this to int would not work very well with FileConverter
    
    In jargo:
    Arguments.fileArgument("-file").parse("-file", "filename.txt").createNewFile();
    
    if fileArgument would change to being integerArgument, trying to call createNewFile() would generate a compile-time error

4. Because JCommander doesn't support repeated arguments other than List<String>.

    String[] args = {"--numbers", "5", "6", "--numbers", "3", "4"};
    Argument<List<List<Integer>>> numbers = Arguments.integerArgument("--numbers").arity(2).repeated().build();

5. Because I love [Guava](https://code.google.com/p/guava-libraries/) and wanted an argument parsing 
    library well integrated with it (more to come in this department)

6. Reflection makes it hard to analyze references to classes/methods and it 
    often requires a granted suppressAccessChecks from the SecurityManager, this may not be wanted. No reflection is used in jargo.

