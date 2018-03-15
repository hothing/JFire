package org.nightlabs.jfire.geography.admin.ui.templatedata.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.id.CSVID;
import org.nightlabs.jfire.geography.ui.GeographyImplClient;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.IOUtil;

public class ExportCSVAction implements IWorkbenchWindowActionDelegate
{
	private IWorkbenchWindow window;

	@Override
	public void dispose() {
		// nothing to dispose
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	private Set<CSVID> getCSVIDs() throws Exception
	{
		GeographyTemplateDataManagerRemote m = JFireEjb3Factory.getRemoteBean(GeographyTemplateDataManagerRemote.class, Login.getLogin().getInitialContextProperties());
		Set<CSVID> csvIDs = m.getCSVIDs();
		return csvIDs;
	}

	@Override
	public void run(IAction action) {
		DirectoryDialog directoryDialog = new DirectoryDialog(window.getShell());
		final String directoryName = directoryDialog.open();
		if (directoryName == null)
			return;

		Job job = new Job(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ExportCSVAction.job.exportGeographyTemplateData")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				final File directory = new File(directoryName);
				if (directory.exists()) {
					if (!directory.isDirectory()) {
						window.getShell().getDisplay().asyncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(window.getShell(),
										Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ExportCSVAction.dialog.cannotCreateDirectory.title"), //$NON-NLS-1$
										String.format(
												Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ExportCSVAction.dialog.cannotCreateDirectory.message"), //$NON-NLS-1$
												directory.getAbsolutePath()
										)
								);
							}
						});
					}
				}
				else {
					directory.mkdirs();
					if (!directory.isDirectory()) {
						window.getShell().getDisplay().asyncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(window.getShell(),
										Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ExportCSVAction.dialog.createDirectoryFailed.title"), //$NON-NLS-1$
										String.format(
												Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ExportCSVAction.dialog.createDirectoryFailed.message"), //$NON-NLS-1$
												directory.getAbsolutePath()
										)
								);
							}
						});
					}
				}

				Set<CSVID> csvIDs = getCSVIDs();
				for (CSVID csvID : csvIDs) {
					InputStream in = GeographyImplClient.createCSVInputStream(csvID.csvType, csvID.countryID);
					try {
						File f = new File(directory, "Data-" + csvID.csvType + (csvID.countryID.isEmpty() ? "" : "-") + csvID.countryID + ".csv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						FileOutputStream out = new FileOutputStream(f);
						try {
							IOUtil.transferStreamData(in, out);
						} finally {
							out.close();
						}
					} finally {
						in.close();
					}
				}

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) { }

}
