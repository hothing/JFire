package org.nightlabs.jfire.base.ui.prop.validation;

import org.nightlabs.jfire.base.idgenerator.IDGeneratorClient;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.validation.IScriptValidator;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructBlockScriptValidatorHandler 
extends AbstractScriptValidatorHandler 
{
	private StructBlock structBlock;
	
	public StructBlockScriptValidatorHandler(StructBlock structBlock) 
	{
		if (structBlock == null)
			throw new IllegalArgumentException("Param structBlock muts not be null!"); //$NON-NLS-1$
		
		this.structBlock = structBlock;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddScriptValidatorHandler#addTemplatePressed()
	 */
	@Override
	public String getTemplateText() 
	{
		final String LINE_BREAK = "\n";  //$NON-NLS-1$
		String key = getScriptValidatorEditor().getCurrentKey();
		StringBuilder sb = new StringBuilder();
		sb.append("importPackage(Packages.org.nightlabs.jfire.prop);"); //$NON-NLS-1$
		sb.append(LINE_BREAK);
		sb.append("importPackage(Packages.org.nightlabs.jfire.prop.id);"); //$NON-NLS-1$
		sb.append(LINE_BREAK);
		for (int i=0; i<structBlock.getStructFields().size(); i++) {
			StructField<?> sf = structBlock.getStructFields().get(i);			
			String structFieldName = getStructFieldVarName(sf);
			sb.append(structFieldName+"ID"); //$NON-NLS-1$
			sb.append(" = "); //$NON-NLS-1$
			sb.append("new StructFieldID("); //$NON-NLS-1$
			sb.append("\""); //$NON-NLS-1$
			sb.append(sf.getStructFieldIDObj().toString());
			sb.append("\""); //$NON-NLS-1$
			sb.append(");"); //$NON-NLS-1$
			sb.append(LINE_BREAK);
			sb.append(structFieldName+" = dataBlock.getDataField("+structFieldName+"ID);"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(LINE_BREAK);
		}
		sb.append("if ("); //$NON-NLS-1$
		for (int i=0; i<structBlock.getStructFields().size(); i++) {			
			StructField<?> structField = structBlock.getStructFields().get(i);
			String structFieldName = getStructFieldVarName(structField);
			sb.append(structFieldName+".isEmpty()"); //$NON-NLS-1$
			if (i != structBlock.getStructFields().size() - 1) {
				sb.append(" || "); //$NON-NLS-1$
			}
		}
		sb.append(")"); //$NON-NLS-1$
		sb.append("{"); //$NON-NLS-1$
		sb.append(LINE_BREAK);
		sb.append("\""+ key +"\";"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(LINE_BREAK);
		sb.append("}"); //$NON-NLS-1$
		sb.append("else {"); //$NON-NLS-1$
		sb.append(LINE_BREAK);
		sb.append("undefined;"); //$NON-NLS-1$
		sb.append(LINE_BREAK);
		sb.append("}"); //$NON-NLS-1$
		return sb.toString();
	}

	private String getStructFieldVarName(StructField<?> sf) {
		String name = sf.getName().getText().trim();
		return name.replace(" ", "-"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String validateScript(String script) 
	{
		IScriptValidator validator = getScriptValidatorEditor().getScriptValidator();
		String oldScript = validator.getScript();
		IStruct struct = structBlock.getStruct();
		try {
			validator.setScript(script);
			if (struct instanceof StructLocal) 
			{
				StructLocal structLocal = (StructLocal) struct; 
				PropertySet propertySet = new PropertySet(IDGeneratorClient.getOrganisationID(), 
						IDGeneratorClient.nextID(PropertySet.class), structLocal);
				propertySet.inflate(struct);
				DataBlockGroup dataBlockGroup;
				try {
					dataBlockGroup = propertySet.getDataBlockGroup(structBlock.getStructBlockIDObj());
					if (dataBlockGroup != null && !dataBlockGroup.isEmpty()) {
						DataBlock dataBlock = dataBlockGroup.getDataBlocks().iterator().next();
						ValidationResult result = validator.validate(dataBlock, structBlock);
						if (result == null) {
							return "result is null"; //$NON-NLS-1$
						}
						return null;
					}
					else {
						return "DataBlockGroup is empty"; //$NON-NLS-1$
					}
				} catch (DataBlockGroupNotFoundException e) {
					return e.getMessage();
				}
			}
			else {
				return "Struct is no StructLocal";	 //$NON-NLS-1$
			}			
		} finally {
			validator.setScript(oldScript);
		}
	}
}
