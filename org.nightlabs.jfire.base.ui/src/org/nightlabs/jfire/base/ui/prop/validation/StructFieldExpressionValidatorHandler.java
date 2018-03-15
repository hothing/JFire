package org.nightlabs.jfire.base.ui.prop.validation;

import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructFieldExpressionValidatorHandler 
extends AbstractExpressionValidatorHandler 
{	
	private StructField<?> structField;
	
	public StructFieldExpressionValidatorHandler(StructField<?> structField) {
		this.structField = structField;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.validation.AbstractAddExpressionValidatorHandler#getStructFieldID()
	 */
	@Override
	protected StructFieldID getStructFieldID() {
		return structField.getStructFieldIDObj();
	}

}
