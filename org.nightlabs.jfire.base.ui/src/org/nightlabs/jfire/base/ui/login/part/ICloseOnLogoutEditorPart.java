package org.nightlabs.jfire.base.ui.login.part;

import org.eclipse.ui.IEditorPart;

/**
 * This is a tagging interface for implementations of {@link IEditorPart}
 * which should be closed when an logout occurs.
 * This is done by the {@link LoginStateListenerForCloseOnLogoutEditorParts} and
 * will be handled automatically. Implementations must therefore not do anything else
 * then implement this interface, to achieve this behaviour.
 *
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public interface ICloseOnLogoutEditorPart
extends IEditorPart
{

}
