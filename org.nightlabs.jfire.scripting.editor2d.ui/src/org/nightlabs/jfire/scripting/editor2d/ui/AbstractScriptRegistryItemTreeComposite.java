/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/
package org.nightlabs.jfire.scripting.editor2d.ui;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.scripting.ui.ScriptRegistryItemNode;
import org.nightlabs.jfire.scripting.ui.ScriptRegistryItemProvider;
import org.nightlabs.jfire.scripting.ui.ScriptRegistryItemTree;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractScriptRegistryItemTreeComposite
extends XComposite
{
	public static final Logger logger = Logger.getLogger(AbstractScriptRegistryItemTreeComposite.class);

	public AbstractScriptRegistryItemTreeComposite(Composite parent, int style)
	{
		super(parent, style);
		createComposite(this);
	}

	public AbstractScriptRegistryItemTreeComposite(Composite parent, int style,
			LayoutMode layoutMode, LayoutDataMode layoutDataMode)
	{
		super(parent, style, layoutMode, layoutDataMode);
		createComposite(this);
	}

	protected ScriptRegistryItemTree scriptTree = null;
	public ScriptRegistryItemTree getScriptTree() {
		return scriptTree;
	}
	
	public void createComposite(Composite parent)
	{
		scriptTree = new ScriptRegistryItemTree(parent, SWT.BORDER | SWT.FULL_SELECTION,
				true, true, getZone(), true);
		scriptTree.setLayoutData(new GridData(GridData.FILL_BOTH));

//		final ScriptRegistryItem loadingDataSRI = new org.nightlabs.jfire.scripting.ScriptCategory(getOrganisationID(), "dummy", "dummy");
//		loadingDataSRI.getName().setText(NLLocale.getDefault().getLanguage(), "Loading data...");
//		ScriptRegistryItemNode loadingDataNode = new ScriptRegistryItemNode(null, new ScriptRegistryItemCarrier(null, loadingDataSRI, false)) {
//			@Override
//			public ScriptRegistryItem getRegistryItem()
//			{
//				return loadingDataSRI;
//			}
//		};
//		scriptTree.setInput(loadingDataNode);
		scriptTree.setInput("Loading data...");

		Job job = new Job("Loading scripts") {
			@Override
			protected IStatus run(ProgressMonitor monitor)
					throws Exception
			{
				ScriptRegistryItemProvider provider = ScriptRegistryItemProvider.sharedInstance();
				final Collection<ScriptRegistryItemNode> scriptNodes = provider.getNodes(getScriptRegistryItemIDs());
				if (!scriptNodes.isEmpty()) {
					for (Iterator<ScriptRegistryItemNode> it = scriptNodes.iterator(); it.hasNext(); ) {
						ScriptRegistryItemNode scriptNode = it.next();
						logger.debug("topLevel scriptNode = " + (scriptNode == null ? null : scriptNode.getName())); //$NON-NLS-1$
						if (scriptNode == null)
							it.remove();
					}
				}
				else {
					logger.debug("topLevel scriptNodes is empty!"); //$NON-NLS-1$
				}

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						if (scriptTree.isDisposed())
							return;

						scriptTree.setInput(scriptNodes);
						scriptTree.getTreeViewer().expandToLevel(2);
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
		job.schedule();
	}
	
	protected String getOrganisationID() {
		return Organisation.DEV_ORGANISATION_ID;
	}
	
	protected abstract Set<ScriptRegistryItemID> getScriptRegistryItemIDs();
	protected abstract String getZone();
}
