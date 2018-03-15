/* *****************************************************************************
 * NightLabs Editor2D - Graphical editor framework                             *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 * Project author: Daniel Mazurek <Daniel.Mazurek [at] nightlabs [dot] org>    *
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

package org.nightlabs.editor2d.ui.figures;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.ZoomListener;
import org.nightlabs.base.ui.util.ColorUtil;
import org.nightlabs.editor2d.ShapeDrawComponent;
import org.nightlabs.editor2d.ShapeDrawComponent.LineStyle;
import org.nightlabs.editor2d.j2d.GeneralShape;
import org.nightlabs.editor2d.ui.util.J2DUtil;
import org.nightlabs.editor2d.viewer.ui.util.AWTSWTUtil;
import org.nightlabs.i18n.unit.resolution.IResolutionUnit;
import org.nightlabs.i18n.unit.resolution.Resolution;
import org.nightlabs.i18n.unit.resolution.ResolutionImpl;

public class ShapeFigure
extends Shape
implements IShapeFigure
{
	public static final double DEFAULT_HIT_TOLERANCE = 5;

	protected Graphics j2d;
	protected Graphics2D g2d;
	protected AffineTransform at = new AffineTransform();
	protected GeneralShape gp;
	protected java.awt.Rectangle gpBounds;
	protected boolean fill = true;;
	private Resolution resolution = null;
	protected double hitTolerance = DEFAULT_HIT_TOLERANCE;
	protected Area outlineArea;

	protected ZoomListener zoomListener = new ZoomListener()
	{
		public void zoomChanged(double zoom)
		{
			hitTolerance = hitTolerance / zoom;
		}
	};

	public ShapeFigure() {
		super();
	}

	@Override
	public void setBounds(Rectangle newBounds)
	{
		repaint();
		super.setBounds(J2DUtil.toDraw2D(getGPBounds()));
	}

	@Override
	public Rectangle getBounds()
	{
		if (getGeneralShape() != null) {
			return J2DUtil.toDraw2D(getGeneralShape().getBounds());
		}
		else {
			return super.getBounds();
		}
	}

	@Override
	protected void fillShape(Graphics graphics)
	{
		if (J2DUtil.instanceofJ2DGraphics(graphics))
		{
			j2d = graphics;
//			g2d = j2d.createGraphics2D();
			g2d = J2DUtil.createGraphics2D(j2d);
			g2d.setClip(null);
			g2d.setPaint(ColorUtil.toAWTColor(getBackgroundColor()));
			g2d.fill(getGeneralShape());
			g2d.dispose();
		}
		else {
			graphics.setAlpha(125);
			graphics.setXORMode(true);
			graphics.setBackgroundColor(getBackgroundColor());
			graphics.fillPath(AWTSWTUtil.convertShape(getGeneralShape(), null, null));			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.Shape#outlineShape(org.eclipse.draw2d.Graphics)
	 */
	@Override
	protected void outlineShape(Graphics graphics)
	{
		if (J2DUtil.instanceofJ2DGraphics(graphics))
		{
			j2d = graphics;
//			g2d = j2d.createGraphics2D();
			g2d = J2DUtil.createGraphics2D(j2d);
			g2d.setClip(null);
			g2d.setPaint(ColorUtil.toAWTColor(getForegroundColor()));
			//      g2d.setStroke(RenderUtil.getStroke(lineWidth, lineStyle));
			g2d.setStroke(ShapeDrawComponent.StrokeUtil.getStroke(lineWidth, convertLineStyle(lineStyle), getResolution()));
			g2d.draw(getGeneralShape());
			g2d.dispose();
		}
		else {
			graphics.setAlpha(125);
			graphics.setXORMode(true);
			graphics.setForegroundColor(getForegroundColor());
			graphics.drawPath(AWTSWTUtil.convertShape(getGeneralShape(), null, null));			
		}
	}

	private LineStyle convertLineStyle(int lineStyle)
	{
		if (lineStyle == 1)
			return LineStyle.SOLID;
		if (lineStyle == 2)
			return LineStyle.DASHED_1;
		if (lineStyle == 3)
			return LineStyle.DASHED_2;
		if (lineStyle == 4)
			return LineStyle.DASHED_3;
		if (lineStyle == 5)
			return LineStyle.DASHED_4;

		return LineStyle.SOLID;
	}

	protected Resolution getResolution()
	{
		if (resolution == null)
			resolution = new ResolutionImpl(IResolutionUnit.dpiUnit, 72);
		return resolution;
	}

	public GeneralShape getGeneralShape() {
		return gp;
	}

	public void setGeneralShape(GeneralShape generalShape) {
		gp = generalShape;
		outlineArea = null;
	}

	public java.awt.Rectangle getGPBounds()
	{
		if (gp == null)
			gpBounds = J2DUtil.toAWTRectangle(getBounds());
		else
			gpBounds = getGeneralShape().getBounds();

		return gpBounds;
	}

	public void transform(AffineTransform at)
	{
		getGeneralShape().transform(at);
		outlineArea = null;
		bounds = J2DUtil.toDraw2D(getGeneralShape().getBounds());
		repaint();
	}

	public double getHitTolerance() {
		return hitTolerance;
	}

	public void setHitTolerance(double hitTolerance) {
		this.hitTolerance = hitTolerance;
	}

	public ZoomListener getZoomListener() {
		return zoomListener;
	}

	/**
	 * @see IFigure#containsPoint(int, int)
	 */
	@Override
	public boolean containsPoint(int x, int y)
	{
		if (isFill())
			return getGeneralShape().contains(x, y);
		else
		{
			if (outlineArea == null) {
				Rectangle outerBounds = getBounds().getCopy();
				Rectangle innerBounds = getBounds().getCopy();
				outerBounds.expand((int)hitTolerance, (int)hitTolerance);
				innerBounds.shrink((int)hitTolerance, (int)hitTolerance);
				GeneralShape outerGS = (GeneralShape) getGeneralShape().clone();
				GeneralShape innerGS = (GeneralShape) getGeneralShape().clone();
				J2DUtil.transformGeneralShape(outerGS, getBounds(), outerBounds);
				J2DUtil.transformGeneralShape(innerGS, getBounds(), innerBounds);
				outlineArea = new Area(outerGS);
				Area innerArea = new Area(innerGS);
				outlineArea.exclusiveOr(innerArea);
			}
			return outlineArea.contains(x,y);
		}
	}

	@Override
	public void performScale(double factor)
	{
		at.setToIdentity();
		at.scale(factor, factor);
		transform(at);
	}

	@Override
	public void performTranslate(int dx, int dy)
	{
		at.setToIdentity();
		at.translate(dx, dy);
		transform(at);
	}

	public GeneralShape getHandleShape() {
		return getGeneralShape();
	}

	/**
	 * Sets whether this shape should fill its region or not. It repaints this figure.
	 *
	 * @param b fill state
	 * @since 2.0
	 */
	@Override
	public void setFill(boolean b) {
		fill = b;
		super.setFill(b);
	}

	public boolean isFill() {
		return fill;
	}

}
