package org.nightlabs.installer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultErrorHandler;

/**
 * The installer command line interface.
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision: 1564 $ - $Date: 2009-01-15 17:36:37 +0100 (Do, 15 Jan 2009) $
 */
public class InstallerCLI
{
	private static final String UI_OPT = "u"; //$NON-NLS-1$
	private static final String UI_LONG_OPT = "ui"; //$NON-NLS-1$
	
	private static final String HELP_OPT = "h"; //$NON-NLS-1$
	private static final String HELP_LONG_OPT = "help"; //$NON-NLS-1$
	
	private static final String CONFIG_OPT = "c"; //$NON-NLS-1$
	private static final String CONFIG_LONG_OPT = "config"; //$NON-NLS-1$
	
	private static final String DEFAULTS_OPT = "d"; //$NON-NLS-1$
	private static final String DEFAULTS_LONG_OPT = "defaults"; //$NON-NLS-1$
	
	private static final String VERBOSE_OPT = "v"; //$NON-NLS-1$
	private static final String VERBOSE_LONG_OPT = "verbose"; //$NON-NLS-1$
	
	private CommandLine commandLine;
	
	/**
	 * Create a new InstallerCLI.
	 * @param args The command line arguments.
	 */
	public InstallerCLI(String[] args)
	{
		commandLine = getCommandLine(args);
	}
	
	/**
	 * Show the help screen and exit the program.
	 */
	public void handleHelp()
	{
		if(commandLine.hasOption(HELP_OPT)) { //$NON-NLS-1$
			HelpFormatter formatter = new HelpFormatter();
			formatter.defaultSyntaxPrefix = Messages.getString("InstallerCLI.syntaxPrefix");
			formatter.printHelp(InstallationManager.class.getName()+Messages.getString("InstallerCLI.usage"), getOptions()); //$NON-NLS-1$
			System.exit(0);
		}
	}
	
	private static void handleError(Throwable e)
	{
		System.err.println(Messages.getString("InstallerCLI.cliError")); //$NON-NLS-1$
		e.printStackTrace();
		new DefaultErrorHandler().handle(new InstallationException(Messages.getString("InstallerCLI.cliError"), e)); //$NON-NLS-1$
		System.exit(1);
	}
	
	private static CommandLine getCommandLine(String[] args)
	{
		try {
			Options options = getOptions();
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);
			return cmd;
		} catch(Throwable e) {
			handleError(e);
			return null;
		}
	}

	/**
	 * Create the command line options for Jakarta commons cli.
	 * @return The command line options
	 */
	private static Options getOptions()
	{
		Options options = new Options();
		Option ui = new Option(UI_OPT, UI_LONG_OPT, true, Messages.getString("InstallerCLI.uiParamDescription")); //$NON-NLS-1$
		options.addOption(ui);
		Option help = new Option(HELP_OPT, HELP_LONG_OPT, false, Messages.getString("InstallerCLI.helpParamDescription")); //$NON-NLS-1$
		options.addOption(help);
		Option config = new Option(CONFIG_OPT, CONFIG_LONG_OPT, true, Messages.getString("InstallerCLI.configParamDescription")); //$NON-NLS-1$
		options.addOption(config);
		Option defaults = new Option(DEFAULTS_OPT, DEFAULTS_LONG_OPT, true, Messages.getString("InstallerCLI.defaultsParamDescription")); //$NON-NLS-1$
		options.addOption(defaults);
		Option verbose = new Option(VERBOSE_OPT, VERBOSE_LONG_OPT, false, Messages.getString("InstallerCLI.verboseParamDescription")); //$NON-NLS-1$
		options.addOption(verbose);
		return options;
	}

	/**
	 * Get the UI type chosen by the command line options.
	 * @return The chosen UI type or {@link UIType#auto} if
	 * 		no type was chosen by the user or the option value
	 * 		is invalid.
	 */
	public UIType getUIType()
	{
		UIType uiType = null;
		if(commandLine.hasOption(UI_OPT)) //$NON-NLS-1$
			uiType = UIType.getByName(commandLine.getOptionValue(UI_OPT)); //$NON-NLS-1$
		if(uiType == null)
			uiType = UIType.auto;
		return uiType;
	}
	
	/**
	 * Get the configuration filename given with the "config" option.
	 * @return The configuration file name if given or <code>null</code> if not.
	 */
	public String getConfigFilename()
	{
		String configFilename = null;
		if(commandLine.hasOption(CONFIG_OPT)) //$NON-NLS-1$
			configFilename = commandLine.getOptionValue(CONFIG_OPT); //$NON-NLS-1$
		return configFilename;
	}
	
	/**
	 * Get the defaults filename given with the "default" option.
	 * @return The defaults filename or <code>null</code> if this option
	 * 		is not available.
	 */
	public String getDefaultsFilename()
	{
		if(commandLine.hasOption(DEFAULTS_OPT)) {
			String filename = commandLine.getOptionValue(DEFAULTS_OPT);
			return filename;
		}
		return null;
	}
	
	public boolean isVerbose()
	{
		return commandLine.hasOption(VERBOSE_OPT);
	}
}
