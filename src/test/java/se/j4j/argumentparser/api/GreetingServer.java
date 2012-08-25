package se.j4j.argumentparser.api;

import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.CommandLineParser.withArguments;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;

import com.google.common.base.Ascii;
import com.google.common.base.Charsets;
import com.google.common.collect.Ranges;

public class GreetingServer
{
	// Statically configured arguments, immutable and therefore also reusable by other parsers
	static final Argument<Boolean> ENABLE_LOGGING = optionArgument("-l", "--enable-logging").description("Output debug information to standard out")
			.build();

	static final Argument<List<Integer>> PORTS = integerArgument("-p", "--listen-port").limitTo(Ranges.closed(0, 65536)).defaultValue(10000)
			.repeated().description("The port clients should connect to").metaDescription("port").build();

	static final Argument<List<String>> GREETING_PHRASES = stringArgument().variableArity()
			.description("A greeting phrase to greet new connections with").build();

	// Immutable and therefore also reusable parser
	static final CommandLineParser parser = withArguments(GREETING_PHRASES, ENABLE_LOGGING, PORTS);

	public static void main(String[] args)
	{
		System.out.println(parser.usage(GreetingServer.class.getSimpleName()));

		GreetingServer server = new GreetingServer();
		server.startWithArgs("--enable-logging", "--listen-port", "8090", "Hello world", "Habla Senor");
		server.startWithArgs("-l");
	}

	void startWithArgs(String ... arguments)
	{
		try
		{
			ParsedArguments args = parser.parse(arguments);

			start(args.get(ENABLE_LOGGING), args.get(PORTS), args.get(GREETING_PHRASES));
		}
		catch(ArgumentException exception)
		{
			System.out.println(exception.getMessageAndUsage(GreetingServer.class.getSimpleName()));
			System.exit(1);
		}
	}

	void start(boolean loggingIsEnabled, List<Integer> ports, List<String> greetingPhrases)
	{
		if(loggingIsEnabled)
		{
			System.out.println("Starting server on port " + ports + ", will greet new connections with " + greetingPhrases);
		}
		try
		{
			for(Integer port : ports)
			{
				ServerSocket server = new ServerSocket(port, 10000);
				Socket client = server.accept();
				Writer writer = new OutputStreamWriter(client.getOutputStream(), Charsets.UTF_8);
				for(String greetingPhrase : greetingPhrases)
				{
					writer.append(greetingPhrase).append((char) Ascii.LF);
				}
				writer.close();
			}
		}
		catch(IOException e)
		{
			throw new RuntimeException("Bad shit happened", e);
		}
	}
}
