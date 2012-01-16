package org.afraid.poison.googlecodeupload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class App {

	public static void main(String[] args) throws MalformedURLException, FileNotFoundException, IOException, UnsupportedEncodingException, URISyntaxException {
		try {
			CommandLineParser parser=new PosixParser();
			Options options=buildOptions();
			CommandLine line=parser.parse(options, args);
			if (line.getArgList().isEmpty()) {
				printHelp();
				System.exit(0);
			}
			Project project=new Project();
			project.setUserName(line.getOptionValue("u"));
			project.setPassword(line.getOptionValue("p"));
			project.setName(line.getOptionValue("n"));
			FileDefinition fileDefinition=new FileDefinition();
			fileDefinition.setFile(new File((String) line.getArgList().get(0)));
			if (line.hasOption("s")) {
				fileDefinition.setSummary(line.getOptionValue("s"));
			}
			if (line.hasOption("l") && null!=line.getOptionValue("l")) {
				fileDefinition.setLabels(new LinkedHashSet<String>(Arrays.asList(line.getOptionValue("l").split(","))));
			} else {
				fileDefinition.setSummary(fileDefinition.getFile().getName());
			}
			if (line.hasOption("filename") && null!=line.getOptionValue("filename")) {
				fileDefinition.setTargetFileName(line.getOptionValue("filename"));
			}
			if (line.hasOption("description") && null!=line.getOptionValue("description")) {
				fileDefinition.setDescription(line.getOptionValue("description"));
			}
			project.upload(fileDefinition);
		} catch (ParseException ex) {
			System.err.println(ex);
			printHelp();
		}
	}

	public static Options buildOptions() {
		Option user=new Option("u", "user", true, "Your Google Code username");
		user.setRequired(true);
		Option password=new Option("p", "password", true, "Your Google Code password");
		password.setRequired(true);
		Option projectName=new Option("n", "project", true, "Google Code project name");
		projectName.setRequired(true);
		Option summary=new Option("s", "summary", true, "Short description of the file");
		Option labels=new Option("l", "labels", true, "An optional list of comma-separated labels to attach to the file");
		Option targetFileName=new Option(null, "filename", true, "optional filename");
		Option description=new Option("d", "description", true, "optional description");

		Options options=new Options();
		options.addOption(user);
		options.addOption(password);
		options.addOption(projectName);
		options.addOption(summary);
		options.addOption(labels);
		options.addOption(targetFileName);
		options.addOption(description);
		return options;
	}

	public static void printHelp() {
		HelpFormatter formatter=new HelpFormatter();
		formatter.printHelp("java -jar googlecode-upload.jar [OPTION]... FILE", buildOptions());
	}
}
