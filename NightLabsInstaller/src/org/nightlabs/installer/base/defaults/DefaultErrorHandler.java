package org.nightlabs.installer.base.defaults;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import org.nightlabs.installer.InstallationManager;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.UIType;
import org.nightlabs.installer.base.ErrorHandler;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.ConsoleFormatter;
import org.nightlabs.installer.base.ui.ConsoleUI;
import org.nightlabs.installer.base.ui.SwingUI;

/**
 * The default {@link ErrorHandler} implementation.
 *
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision: 1567 $ - $Date: 2009-01-17 14:54:35 +0100 (Sa, 17 Jan 2009) $
 */
public class DefaultErrorHandler extends DefaultConfigurable implements ErrorHandler
{
	private boolean verbose;
	
	private String getAllCausesMessage(Throwable e)
	{
		StringBuffer sb = new StringBuffer();
		Throwable rootCause = null;
		while(e != null) {
			rootCause = e;
			if(sb.length() > 0)
				sb.append("\n");
			String msg = e.getLocalizedMessage();
			if(verbose || msg == null || !InstallationException.class.isAssignableFrom(e.getClass())) {
				sb.append(e.getClass().getName());
				if(msg != null)
					sb.append(": ");
			}
			if(msg != null) {
				sb.append(msg);
			}
			e = e.getCause();
		}
		if(verbose && rootCause != null) {
			// append stack trace in verbose mode
			sb.append("\n\nRoot cause stack trace:\n");
			StringWriter sw = new StringWriter();
			rootCause.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			stacktrace.replace("\t", "");
			sb.append(stacktrace);
		}
		return sb.toString();
	}

	public void handle(Throwable e)
	{
		handle(e, true);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.ErrorHandler#handle(java.lang.Throwable, boolean)
	 */
	public void handle(Throwable e, boolean exit)
	{
		try {
			String msg = String.format(Messages.getString("DefaultErrorHandler.errorMessage"), getAllCausesMessage(e)); //$NON-NLS-1$
			msg = new ConsoleFormatter().getWrapped(msg);
			InstallationManager installationManager = InstallationManager.getInstallationManager();
			if(installationManager.getUiType() == UIType.swing) {
				JOptionPane.showMessageDialog(SwingUI.getInstallerFrame(), msg, Messages.getString("DefaultErrorHandler.errorLabel"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			}
			if(installationManager.getUiType() == UIType.console) {
				ConsoleFormatter consoleFormatter = ConsoleUI.getConsoleFormatter();
				consoleFormatter.println();
				consoleFormatter.println(msg);
				consoleFormatter.println();
			} else {
				e.printStackTrace();
			}
		} finally {
			if(exit)
				System.exit(1);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.ErrorHandler#setVerbose(boolean)
	 */
	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}
	
	/**
	 * Is this error handler in verbose mode?
	 * @return <code>true</code> if verbose mode is on - <code>false</code> otherwise
	 */
	public boolean isVerbose()
	{
		return verbose;
	}
}
