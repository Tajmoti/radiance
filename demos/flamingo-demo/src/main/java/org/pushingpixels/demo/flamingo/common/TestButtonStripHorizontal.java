/*
 * Copyright (c) 2005-2018 Flamingo Kirill Grouchnikov. All Rights Reserved.
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
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of
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
package org.pushingpixels.demo.flamingo.common;

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import org.pushingpixels.demo.flamingo.LocaleSwitcher;
import org.pushingpixels.demo.flamingo.svg.logo.RadianceLogo;
import org.pushingpixels.demo.flamingo.svg.tango.transcoded.*;
import org.pushingpixels.flamingo.api.common.CommandButtonPresentationState;
import org.pushingpixels.flamingo.api.common.model.*;
import org.pushingpixels.flamingo.api.common.projection.CommandStripProjection;
import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.skin.BusinessSkin;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class TestButtonStripHorizontal extends JFrame {
    private Locale currLocale;

    private JPanel buttonPanel;

    private TestButtonStripHorizontal() {
        super("Tests for horizontal buttons strips");

        this.setIconImage(RadianceLogo.getLogoImage(
                SubstanceCortex.GlobalScope.getCurrentSkin().getColorScheme(
                        SubstanceSlices.DecorationAreaType.PRIMARY_TITLE_PANE,
                        SubstanceSlices.ColorSchemeAssociationKind.FILL,
                        ComponentState.ENABLED)));

        buttonPanel = getButtonPanel();
        this.add(buttonPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JComboBox localeSwitcher = LocaleSwitcher.getLocaleSwitcher((Locale selected) -> {
            currLocale = selected;
            remove(buttonPanel);
            buttonPanel = getButtonPanel();
            add(buttonPanel, BorderLayout.CENTER);
            Window window = SwingUtilities.getWindowAncestor(buttonPanel);
            window.applyComponentOrientation(ComponentOrientation.getOrientation(currLocale));
            SwingUtilities.updateComponentTreeUI(window);
        });
        controlPanel.add(localeSwitcher);
        this.add(controlPanel, BorderLayout.SOUTH);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private JPanel getButtonPanel() {
        FormBuilder builder = FormBuilder.create().
                columns("right:pref, 4dlu, left:pref, 4dlu, left:pref").
                rows("p, $lg, p, $lg, p, $lg, p, $lg, p, $lg, p").
                padding(Paddings.DIALOG);

        builder.add("hgap 0.5,  vgap 1.0").xy(1, 1).add(getStrip1(0.5, 1.0)).xy(3, 1).add(
                getStrip2(0.5, 1.0)).xy(5, 1);
        builder.add("hgap 0.75,  vgap 1.0").xy(1, 3).add(getStrip1(0.75, 1.0)).xy(3, 3).add(
                getStrip2(0.75, 1.0)).xy(5, 3);
        builder.add("hgap 1.0,  vgap 1.0").xy(1, 5).add(getStrip1(1.0, 1.0)).xy(3, 5).add(
                getStrip2(1.0, 1.0)).xy(5, 5);

        builder.add("hgap 0.75,  vgap 0.5").xy(1, 7).add(getStrip1(0.75, 0.5)).xy(3, 7).add(
                getStrip2(0.75, 0.5)).xy(5, 7);
        builder.add("hgap 0.75,  vgap 0.75").xy(1, 9).add(getStrip1(0.75, 0.75)).xy(3, 9).add(
                getStrip2(0.75, 0.75)).xy(5, 9);
        builder.add("hgap 0.75,  vgap 1.0").xy(1, 11).add(getStrip1(0.75, 1.0)).xy(3, 11).add(
                getStrip2(0.75, 1.0)).xy(5, 11);

        return builder.build();
    }

    private JComponent getStrip1(double hgapScaleFactor, double vgapScaleFactor) {
        return new CommandStripProjection(
                new CommandGroupModel(
                        Command.builder().setIcon(new Format_justify_left()).build(),
                        Command.builder().setIcon(new Format_justify_center()).build(),
                        Command.builder().setIcon(new Format_justify_right()).build()),
                CommandStripPresentationModel.builder()
                        .setCommandPresentationState(CommandButtonPresentationState.SMALL)
                        .setOrientation(CommandStripPresentationModel.StripOrientation.HORIZONTAL)
                        .setHorizontalGapScaleFactor(hgapScaleFactor)
                        .setVerticalGapScaleFactor(vgapScaleFactor)
                        .build())
                .buildComponent();
    }

    private JComponent getStrip2(double hgapScaleFactor, double vgapScaleFactor) {
        return new CommandStripProjection(
                new CommandGroupModel(
                        Command.builder().setIcon(new Format_text_bold()).build(),
                        Command.builder().setIcon(new Format_text_italic()).build(),
                        Command.builder().setIcon(new Format_text_underline()).build(),
                        Command.builder()
                                .setIcon(new Format_text_strikethrough())
                                .setPopupMenuContentModel(SamplePopupMenu.getSamplePopupMenuContentModel())
                                .build()),
                CommandStripPresentationModel.builder()
                        .setCommandPresentationState(CommandButtonPresentationState.SMALL)
                        .setOrientation(CommandStripPresentationModel.StripOrientation.HORIZONTAL)
                        .setHorizontalGapScaleFactor(hgapScaleFactor)
                        .setVerticalGapScaleFactor(vgapScaleFactor)
                        .build())
                .buildComponent();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame.setDefaultLookAndFeelDecorated(true);
            SubstanceCortex.GlobalScope.setSkin(new BusinessSkin());

            new TestButtonStripHorizontal().setVisible(true);
        });
    }
}
