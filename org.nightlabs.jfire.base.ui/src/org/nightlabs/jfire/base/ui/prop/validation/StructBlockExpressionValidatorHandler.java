/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.validation;

import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructBlockExpressionValidatorHandler 
extends AbstractExpressionValidatorHandler
{
	private StructBlock structBlock;
	
	public StructBlockExpressionValidatorHandler(StructBlock structBlock) {
		this.structBlock = structBlock;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.AbstractAddExpressionValidatorHandler#getStructFieldID()
	 */
	@Override
	protected StructFieldID getStructFieldID() 
	{
		if (!structBlock.getStructFields().isEmpty()) {
			StructField<?> structField = structBlock.getStructFields().iterator().next();
			return structField.getStructFieldIDObj();
		}
		return null;
	}
	
}
