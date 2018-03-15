/**
 *
 */
package org.nightlabs.clientui.ui.layout;

import org.apache.log4j.Logger;

/**
 * Helper class to create SWT GridLayout and GridData from NightLabsClientUI objects.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class GridLayoutUtil {

	private static final Logger logger = Logger.getLogger(GridLayoutUtil.class);

	/**
	 * Creates a new SWT GridLayout with the properties from the given NightLabsClientUI GridLayout.
	 *
	 * @param pGridLayout The NightLabsClientUI GridLayout to create a new SWT GridLayout for.
	 * @return A new SWT GridLayout with the properties from the given NightLabsClientUI GridLayout.
	 */
	public static org.eclipse.swt.layout.GridLayout createGridLayout(org.nightlabs.clientui.layout.GridLayout pGridLayout) {
		org.eclipse.swt.layout.GridLayout result = new org.eclipse.swt.layout.GridLayout();
		if (pGridLayout == null) {
			logger.warn("Attempt to get an SWT GridLayout for an null ClientUI GridLayout, will return default GridLayout.", new Exception()); //$NON-NLS-1$
			return result;
		}
		result.horizontalSpacing = pGridLayout.getHorizontalSpacing();
		result.makeColumnsEqualWidth = pGridLayout.isMakeColumnsEqualWidth();
		result.marginBottom = pGridLayout.getMarginBottom();
		result.marginHeight = pGridLayout.getMarginHeight();
		result.marginLeft = pGridLayout.getMarginLeft();
		result.marginRight = pGridLayout.getMarginRight();
		result.marginTop = pGridLayout.getMarginTop();
		result.marginWidth = pGridLayout.getMarginWidth();
		result.numColumns = pGridLayout.getNumColumns();
		result.verticalSpacing = pGridLayout.getVerticalSpacing();
		return result;
	}

	/**
	 * Creates a new SWT GridData with the properties from the given NightLabsClientUI GridData.
	 *
	 * @param pGridData The NightLabsClientUI GridData to create a new SWT GridData for.
	 * @return A new SWT GridData with the properties from the given NightLabsClientUI GridData.
	 */
	public static org.eclipse.swt.layout.GridData createGridData(org.nightlabs.clientui.layout.GridData pGridData) {
		org.eclipse.swt.layout.GridData result = new org.eclipse.swt.layout.GridData();
		if (pGridData == null) {
			logger.warn("Attempt to get an SWT GridData for an null ClientUI GridData, will return default GridData.", new Exception()); //$NON-NLS-1$
			return result;
		}
		result.horizontalAlignment = pGridData.getHorizontalAlignment();
		result.horizontalIndent = pGridData.getHorizontalIndent();
		result.horizontalSpan = pGridData.getHorizontalSpan();
		result.grabExcessHorizontalSpace = pGridData.isGrabExcessHorizontalSpace();
		result.verticalAlignment = pGridData.getVerticalAlignment();
		result.verticalIndent = pGridData.getVerticalIndent();
		result.verticalSpan = pGridData.getVerticalSpan();
		result.grabExcessVerticalSpace = pGridData.isGrabExcessVerticalSpace();
		result.heightHint = pGridData.getHeightHint();
		result.widthHint = pGridData.getWidthHint();
		result.minimumHeight = pGridData.getMinimumHeight();
		result.minimumWidth = pGridData.getMinimumWidth();
		return result;
	}
}
