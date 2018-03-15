package org.nightlabs.jfire.base.ui.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.language.LanguageChooserList;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.language.LanguageCf;
import org.nightlabs.util.IOUtil;

/**
 * @author Alexander Bieber <!-- alex [at] nightlabs [dot] de -->
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SwitchLanguageDialog extends ResizableTitleAreaDialog {

	private LanguageChooserList languageChooser;

	/**
	 * Create a new SwitchLanguageDialog instance.
	 * @param parentShell The parent shell
	 */
	public SwitchLanguageDialog(Shell parentShell) {
		super(parentShell, Messages.getBundle());
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.getString("org.nightlabs.jfire.base.ui.language.SwitchLanguageDialog.titleAreaTitle")); //$NON-NLS-1$
		setMessage(Messages.getString("org.nightlabs.jfire.base.ui.language.SwitchLanguageDialog.titleAreaMessage")); //$NON-NLS-1$

		Composite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER);
//		Label l = new Label(wrapper, SWT.WRAP);
//		l.setText(Messages.getString("org.nightlabs.jfire.base.ui.language.SwitchLanguageDialog.label.selectLanguage")); //$NON-NLS-1$
//		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		languageChooser = new LanguageChooserList(wrapper);
		languageChooser.setLayoutData(new GridData(GridData.FILL_BOTH));
		languageChooser.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		return wrapper;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		LanguageCf langCf = languageChooser.getLanguage();
		String languageID = langCf.getLanguageID();
		super.okPressed();
		if (LanguageManager.sharedInstance().getCurrentLanguageID().equals(languageID))
			return; // nothing to do, same language
		switchLanguage(languageID);
		boolean doRestart = MessageDialog.openQuestion(getShell(), Messages.getString("org.nightlabs.jfire.base.ui.language.SwitchLanguageDialog.restartQuestionTitle"), Messages.getString("org.nightlabs.jfire.base.ui.language.SwitchLanguageDialog.restartQuestionMessage")); //$NON-NLS-1$ //$NON-NLS-2$
		if(doRestart)
			PlatformUI.getWorkbench().restart();
	}

	private void switchLanguage(String languageID)
	{
		LanguageManager.sharedInstance().setLanguageID(languageID);

		try {
			String launcher = null;
			String eclipseCommands = System.getProperty("eclipse.commands"); //$NON-NLS-1$
			if(eclipseCommands != null) {
				String[] lines = eclipseCommands.split("\n"); //$NON-NLS-1$
				if(lines != null) {
					for (int i=0; i<lines.length; i++) {
						if(lines[i].equals("-launcher") && lines.length > i+1) //$NON-NLS-1$
							launcher = lines[i+1];
					}
				}
			}
			if(launcher != null) {
				String launcherBaseName = launcher;
				if(launcherBaseName.toLowerCase().endsWith(".exe")) //$NON-NLS-1$
					launcherBaseName = launcherBaseName.substring(0, launcherBaseName.length()-".exe".length()); //$NON-NLS-1$
				String ini = launcher+".ini"; //$NON-NLS-1$
				File iniFile = new File(ini);
				if(!iniFile.canWrite())
					throw new IOException("Cannot write to ini file: "+iniFile.getAbsolutePath()); //$NON-NLS-1$

				// backup:
				File backupFile = new File(ini+".bak"); //$NON-NLS-1$
				int count = 1;
				while(backupFile.exists()) {
					backupFile = new File(ini+".bak"+count); //$NON-NLS-1$
					count++;
				}
				IOUtil.copyFile(iniFile, backupFile);

				// read and update contents in memory
				StringBuffer newContents = new StringBuffer();
				BufferedReader fileReader = new BufferedReader(new FileReader(iniFile));
				String line = null;
				boolean replaceNextLine = false;
				boolean foundEntry = false;
				boolean foundVMArgs = false;
				while((line = fileReader.readLine()) != null) {
					if(!foundVMArgs && line.trim().equals("-vmargs")) { //$NON-NLS-1$
						foundVMArgs = true;
					}
					if(!foundVMArgs && replaceNextLine) {
						newContents.append(languageID);
						replaceNextLine = false;
					} else {
						if(!foundVMArgs && line.trim().equals("-nl")) { //$NON-NLS-1$
							replaceNextLine = true;
							foundEntry = true;
						}
						newContents.append(line);
					}
					newContents.append('\n');
				}
				fileReader.close();
				if(!foundEntry) {
					newContents.insert(0, '\n');
					newContents.insert(0, languageID);
					newContents.insert(0, "-nl\n"); //$NON-NLS-1$
				}
				// write new contents
				FileWriter fileWriter = new FileWriter(iniFile);
				fileWriter.write(newContents.toString());
				fileWriter.close();
			}
		} catch(Exception e) {
			Logger.getLogger(SwitchLanguageDialog.class).error("Error updating launcher ini file", e); //$NON-NLS-1$
			MessageDialog.openWarning(getShell(), Messages.getString("org.nightlabs.jfire.base.ui.language.SwitchLanguageDialog.errorDialog.title"), Messages.getString("org.nightlabs.jfire.base.ui.language.SwitchLanguageDialog.errorDialog.message")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("org.nightlabs.jfire.base.ui.language.SwitchLanguageDialog.window.title")); //$NON-NLS-1$
		//newShell.setSize(300, 300);
//		setToCenteredLocationPreferredSize(newShell, 300, 300);
	}



}
