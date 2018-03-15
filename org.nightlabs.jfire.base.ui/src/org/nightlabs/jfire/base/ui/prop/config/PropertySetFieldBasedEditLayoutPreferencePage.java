/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite;
import org.nightlabs.clientui.ui.layout.IGridDataEntry;
import org.nightlabs.clientui.ui.layout.IGridLayoutConfig;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.StaticI18nText;
import org.nightlabs.jfire.base.ui.config.AbstractUserConfigModulePreferencePage;
import org.nightlabs.jfire.base.ui.config.IConfigModuleController;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutUseCase;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Prefrence-page for {@link PropertySetFieldBasedEditLayoutConfigModule}
 * that lets the user configure the layout of a field-based property-set editor.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class PropertySetFieldBasedEditLayoutPreferencePage extends AbstractUserConfigModulePreferencePage {

	/**
	 * {@link IGridLayoutConfig} operating on a {@link PropertySetFieldBasedEditLayoutConfigModule}
	 */
	class GridLayoutConfig implements IGridLayoutConfig {

		private PropertySetFieldBasedEditLayoutConfigModule cfMod;
		private StructLocalID structLocalID;
		private StructLocal structLocal;
		
		private Job loadStructLocalJob;
		
		public GridLayoutConfig(PropertySetFieldBasedEditLayoutConfigModule cfMod, StructLocalID structLocalID) {
			this.cfMod = cfMod;
			this.structLocalID = structLocalID;
			loadStructLocalJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.prop.config.PropertySetFieldBasedEditLayoutPreferencePage.job.loadStructLocal")) { //$NON-NLS-1$
				@Override
				protected IStatus run(ProgressMonitor monitor) throws Exception {
					structLocal = StructLocalDAO.sharedInstance().getStructLocal(GridLayoutConfig.this.structLocalID, monitor);
					return Status.OK_STATUS;
				}
			};
			loadStructLocalJob.schedule();
		}
		
		@Override
		public IGridDataEntry addGridDataEntry() {
			try {
				loadStructLocalJob.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			Set<StructFieldID> ignoreIDs = new HashSet<StructFieldID>();
			for (IGridDataEntry gdEntry : getGridDataEntries()) {
				PropertySetFieldBasedEditLayoutEntry entry = entriesMap.get(gdEntry);
				if (entry != null && PropertySetFieldBasedEditLayoutEntry.ENTRY_TYPE_STRUCT_FIELD_REFERENCE.equals(entry.getEntryType())) {
					if (entry.getStructFieldID() != null)
						ignoreIDs.add(entry.getStructFieldID());
				}
			}
			AddStructFieldEntryDialog dlg = new AddStructFieldEntryDialog(getShell(), null, ignoreIDs, structLocal);
			if (dlg.open() != Window.OK) {
				return null;
			}
			PropertySetFieldBasedEditLayoutEntry layoutEntry = cfMod.createEditLayoutEntry(dlg.getEntryType());
			layoutEntry.setGridData(new GridData(IDGenerator.nextID(GridData.class)));
			if (PropertySetFieldBasedEditLayoutEntry.ENTRY_TYPE_STRUCT_FIELD_REFERENCE.equals(dlg.getEntryType())) {
				layoutEntry.setStructFieldID(dlg.getStructFieldID());
			}
			cfMod.addEditLayoutEntry(layoutEntry);
			IGridDataEntry gdEntry = createGridDataEntry(layoutEntry);
			clearCache();
			return gdEntry;
		}

		private IGridDataEntry createGridDataEntry(final PropertySetFieldBasedEditLayoutEntry entry) {
			IGridDataEntry gdEntry = new IGridDataEntry() {
				@Override
				public GridData getGridData() {
					return entry.getGridData();
				}

				@Override
				public I18nText getName() {
					if (entry.getStructFieldID() == null) {
						return new StaticI18nText("Separator"); //$NON-NLS-1$
					}
					try {
						loadStructLocalJob.join();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					StructField<?> field = null;
					try {
						field = structLocal.getStructField(entry.getStructFieldID());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					return field.getName();
				}
				
			};
			createdEntriesMap.put(entry, gdEntry);
			entriesMap.put(gdEntry, entry);
			return gdEntry;
		}
		
		private List<IGridDataEntry> entries;
		private Map<IGridDataEntry, PropertySetFieldBasedEditLayoutEntry> entriesMap;
		private Map<PropertySetFieldBasedEditLayoutEntry, IGridDataEntry> createdEntriesMap;
		
		@Override
		public List<IGridDataEntry> getGridDataEntries() {
			if (entries == null) {
				entries = new LinkedList<IGridDataEntry>();
				if (entriesMap == null) {
					entriesMap = new HashMap<IGridDataEntry, PropertySetFieldBasedEditLayoutEntry>();
					createdEntriesMap = new HashMap<PropertySetFieldBasedEditLayoutEntry, IGridDataEntry>();
				}
				
				for (final PropertySetFieldBasedEditLayoutEntry entry : cfMod.getEditLayoutEntries()) {
					IGridDataEntry gdEntry = createdEntriesMap.get(entry);
					if (gdEntry == null) {
						gdEntry = createGridDataEntry(entry);
					}
					entries.add(gdEntry);
				}
			}
			return entries;
		}

		@Override
		public GridLayout getGridLayout() {
			return cfMod.getGridLayout();
		}

		@Override
		public boolean moveEntryDown(IGridDataEntry gridDataEntry) {
			if (entriesMap == null)
				return false;
			PropertySetFieldBasedEditLayoutEntry entry = entriesMap.get(gridDataEntry);
			clearCache();
			if (entry == null)
				return false;
			return cfMod.moveEditLayoutEntryDown(entry);
		}

		@Override
		public boolean moveEntryUp(IGridDataEntry gridDataEntry) {
			if (entriesMap == null)
				return false;
			PropertySetFieldBasedEditLayoutEntry entry = entriesMap.get(gridDataEntry);
			clearCache();
			if (entry == null)
				return false;
			return cfMod.moveEditLayoutEntryUp(entry);
		}

		@Override
		public void removeGridDataEntry(IGridDataEntry gridDataEntry) {
			if (entriesMap == null)
				return;
			PropertySetFieldBasedEditLayoutEntry entry = entriesMap.get(gridDataEntry);
			clearCache();
			if (entry == null)
				return;
			cfMod.removeEditLayoutEntry(entry);
		}
		
		protected void clearCache() {
			entries = null;
		}
	}
	
	private GridLayoutConfigComposite configComposite;
	
	private StructLocalID structLocalID;
	private PropertySetFieldBasedEditLayoutUseCase editLayoutUseCase;
	
	
	/**
	 * Create a new {@link PropertySetFieldBasedEditLayoutPreferencePage} for the 
	 * given {@link PropertySetFieldBasedEditLayoutUseCase}.
	 * 
	 */
	public PropertySetFieldBasedEditLayoutPreferencePage(PropertySetFieldBasedEditLayoutUseCase editLayoutUseCase) {
		setStructLocalID(editLayoutUseCase.getStructLocalID());
		this.editLayoutUseCase = editLayoutUseCase;
	}

	public void setStructLocalID(StructLocalID structLocalID) {
		this.structLocalID = structLocalID;
	}
	
	@Override
	protected String getConfigModuleID() {
		return editLayoutUseCase.getUseCaseID();
//		// TODO: This needs to be abstract and subclasses need to define
//		// alternatively we might find a way add new PreferencePages per code and add one for each usecase on the server
//		return PropertySetFieldBasedEditConstants.USE_CASE_ID_EDIT_PERSON;
////		if (structLocalID == null)
////			throw new IllegalStateException("The StructLocal for this " + PropertySetFieldBasedEditLayoutPreferencePage.class.getSimpleName() + " was not set, can not create a cfModID");
////		return PropertySetFieldBasedEditLayoutConfigModule.getStructLocalCfModID(structLocalID);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage#createConfigModuleController()
	 */
	@Override
	protected IConfigModuleController createConfigModuleController() {
		return new PropertySetFieldBasedEditLayoutConfigModuleController(this);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage#createPreferencePage(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createPreferencePage(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.None, LayoutMode.TIGHT_WRAPPER);
		I18nText description = editLayoutUseCase.getDescription();
		if (description != null && description.getText() != null && !description.getText().isEmpty()) {
			Label desc = new Label(wrapper, SWT.WRAP);
			desc.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
			desc.setText(description.getText());
			Label sep = new Label(wrapper, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
		}
		configComposite = new GridLayoutConfigComposite(wrapper, SWT.NONE);
		configComposite.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setConfigChanged(true);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage#updateConfigModule()
	 */
	@Override
	public void updateConfigModule() {
		getConfigModuleController().getConfigModule();
		configComposite.updateGridLayoutConfig();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage#updatePreferencePage()
	 */
	@Override
	protected void updatePreferencePage() {
		configComposite.setGridLayoutConfig(
				new GridLayoutConfig(
						(PropertySetFieldBasedEditLayoutConfigModule) getConfigModuleController().getConfigModule(),
						structLocalID));
	}

}
