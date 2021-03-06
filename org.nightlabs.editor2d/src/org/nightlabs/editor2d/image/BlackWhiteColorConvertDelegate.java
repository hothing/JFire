/* *****************************************************************************
 * NightLabs Editor2D - Graphical editor framework                             *
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
package org.nightlabs.editor2d.image;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import org.nightlabs.editor2d.ImageDrawComponent;
import org.nightlabs.editor2d.render.RenderConstants;
import org.nightlabs.editor2d.util.ImageUtil;

/**
 * @author Daniel.Mazurek <at> NightLabs <dot> de
 *
 */
public class BlackWhiteColorConvertDelegate
implements ImageRendererDelegate
//extends AbstractColorConvertDelegate
{

//	@Override
//	protected BufferedImageOp getBufferedImageOp(RenderModeMetaData metaData) {
//		return new ColorConvertOp(ImageUtil.COLOR_MODEL_BLACK_WHITE.getColorSpace(), getRenderingHints(metaData));
//	}
//
//	@Override
//	public String getRenderMode() {
//		return RenderConstants.BLACK_WHITE_MODE;
//	}

	public RenderedImage render(String renderMode,
			ImageDrawComponent imageDrawComponent, RenderedImage image,
			RenderModeMetaData renderModeMetaData)
	{
		if (image == null)
			image = imageDrawComponent.getOriginalImage();

		if (renderMode.equals(RenderConstants.BLACK_WHITE_MODE))
			return convertBlackWhite(image);
		
		return image;
	}
	
	protected BufferedImage convertBlackWhite(RenderedImage src)
	{
		BufferedImage bi = ImageUtil.convertToBufferedImage(src);
		return ImageUtil.cloneImage(bi, BufferedImage.TYPE_BYTE_BINARY);
	}
	
}
