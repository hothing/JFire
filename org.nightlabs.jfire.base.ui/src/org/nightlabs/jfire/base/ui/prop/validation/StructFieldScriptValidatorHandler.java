/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.validation;

import org.nightlabs.jfire.base.idgenerator.IDGeneratorClient;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.validation.IScriptValidator;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructFieldScriptValidatorHandler 
extends AbstractScriptValidatorHandler 
{
	private StructField<?> structField;
	
	public StructFieldScriptValidatorHandler(StructField<?> structField) {
		this.structField = structField;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddScriptValidatorHandler#getTemplateText()
	 */
	@Override
	public String getTemplateText() 
	{
		final String LINE_BREAK = "\n";  //$NON-NLS-1$
		String key = getScriptValidatorEditor().getCurrentKey();
		StringBuilder sb = new StringBuilder();
		sb.append("importPackage(Packages.org.nightlabs.jfire.prop);" + LINE_BREAK); //$NON-NLS-1$
		sb.append("if (dataField.isEmpty()) {" + LINE_BREAK); //$NON-NLS-1$
		sb.append("\""+ key +"\";" + LINE_BREAK); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("}" + LINE_BREAK); //$NON-NLS-1$
		sb.append("else {" + LINE_BREAK); //$NON-NLS-1$
		sb.append("	undefined" + ";" + LINE_BREAK); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("}"); //$NON-NLS-1$
		return sb.toString();
	}
	
	@Override
	public String validateScript(String script) 
	{
		IScriptValidator validator = getScriptValidatorEditor().getScriptValidator();
		String oldScript = validator.getScript();
		try {
			validator.setScript(script);
			IStruct struct = structField.getStructBlock().getStruct();
			if (struct instanceof StructLocal) 
			{ 
				StructLocal structLocal = (StructLocal) struct; 
				PropertySet propertySet = new PropertySet(IDGeneratorClient.getOrganisationID(), 
						IDGeneratorClient.nextID(PropertySet.class), structLocal);
				propertySet.inflate(struct);
				DataField dataField;
				try {
					dataField = propertySet.getDataField(structField.getStructFieldIDObj());
					if (dataField != null) {
						ValidationResult result = validator.validate(dataField, structField);
						if (result == null) {
							return "result is null"; //$NON-NLS-1$
						}
						return null;
					}
					else {
						return "dataField is null"; //$NON-NLS-1$
					}
				} catch (Exception e) {
					return e.getMessage();
				}
			} else {
				return "Struct is no StructLocal";	 //$NON-NLS-1$
			}			
		} finally {
			validator.setScript(oldScript);
		}
	}
}
