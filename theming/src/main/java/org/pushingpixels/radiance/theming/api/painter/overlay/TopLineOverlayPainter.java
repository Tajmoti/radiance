/*
 * Copyright (c) 2005-2022 Radiance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of the copyright holder nor the names of
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.radiance.theming.api.painter.overlay;

import org.pushingpixels.radiance.theming.api.RadianceThemingSlices;
import org.pushingpixels.radiance.theming.api.RadianceSkin;
import org.pushingpixels.radiance.theming.api.colorscheme.ColorSchemeSingleColorQuery;
import org.pushingpixels.radiance.theming.api.colorscheme.RadianceColorScheme;
import org.pushingpixels.radiance.theming.internal.utils.RadianceCoreUtilities;
import org.pushingpixels.radiance.theming.internal.utils.RadianceSizeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Overlay painter that paints a single line at the top edge of the relevant
 * decoration area. This class is part of officially supported API.
 * 
 * @author Kirill Grouchnikov
 */
public final class TopLineOverlayPainter implements RadianceOverlayPainter {
	/**
	 * Used to compute the color of the line painted by this overlay painter.
	 */
	ColorSchemeSingleColorQuery colorSchemeQuery;

	/**
	 * Creates a new overlay painter that paints a single line at the top edge
	 * of the relevant decoration area
	 * 
	 * @param colorSchemeQuery
	 *            Used to compute the color of the line painted by this overlay
	 *            painter.
	 */
	public TopLineOverlayPainter(ColorSchemeSingleColorQuery colorSchemeQuery) {
		this.colorSchemeQuery = colorSchemeQuery;
	}

	@Override
	public void paintOverlay(Graphics2D graphics, Component comp,
                             RadianceThemingSlices.DecorationAreaType decorationAreaType, int width, int height,
                             RadianceSkin skin) {
		Component topMostWithSameDecorationAreaType = RadianceCoreUtilities
				.getTopMostParentWithDecorationAreaType(comp, decorationAreaType);

		Point inTopMost = SwingUtilities.convertPoint(comp, new Point(0, 0),
				topMostWithSameDecorationAreaType);
		int dy = inTopMost.y;

		float borderStrokeWidth = RadianceSizeUtils.getBorderStrokeWidth(comp);
		graphics.setStroke(new BasicStroke(borderStrokeWidth));

		RadianceColorScheme colorScheme = skin
				.getBackgroundColorScheme(decorationAreaType);
		graphics.setColor(this.colorSchemeQuery.query(colorScheme));
		float topY = borderStrokeWidth - dy;
		Line2D.Float line = new Line2D.Float(0, topY, width, topY);
		graphics.draw(line);
	}

	@Override
	public String getDisplayName() {
		return "Top Line";
	}

}
