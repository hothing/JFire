package org.nightlabs.history.ui;


/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class EditorHistoryForwardActionHandler extends EditorHistoryActionHandler {

	/**
	 * @param action
	 */
	public EditorHistoryForwardActionHandler() {
		super(EditorHistory.sharedInstance().getForwardAction());
	}

}
