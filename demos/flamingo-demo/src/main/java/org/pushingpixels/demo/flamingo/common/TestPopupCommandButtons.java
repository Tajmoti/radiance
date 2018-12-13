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
import org.pushingpixels.demo.flamingo.svg.logo.RadianceLogo;
import org.pushingpixels.demo.flamingo.svg.tango.transcoded.*;
import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.icon.EmptyResizableIcon;
import org.pushingpixels.flamingo.api.common.model.*;
import org.pushingpixels.flamingo.api.common.popup.model.*;
import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.skin.BusinessSkin;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;

public class TestPopupCommandButtons extends JFrame {
    private enum PopupKind {
        SIMPLE, SCROLLABLE, COMPLEX;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private ResourceBundle resourceBundle;

    private Locale currLocale;

    private Command popupSimpleCommand;
    private Command popupScrollableCommand;
    private Command popupComplexCommand;

    private TestPopupCommandButtons() {
        super("Command button test");
        this.setIconImage(RadianceLogo.getLogoImage(
                SubstanceCortex.GlobalScope.getCurrentSkin().getColorScheme(
                        SubstanceSlices.DecorationAreaType.PRIMARY_TITLE_PANE,
                        SubstanceSlices.ColorSchemeAssociationKind.FILL,
                        ComponentState.ENABLED)));

        this.setLayout(new BorderLayout());

        currLocale = Locale.getDefault();
        resourceBundle = ResourceBundle
                .getBundle("org.pushingpixels.demo.flamingo.resource.Resources", currLocale);

        this.popupSimpleCommand = Command.builder()
                .setText(resourceBundle.getString("SelectAll.text"))
                .setIconFactory(Edit_paste.factory())
                .setExtraText(resourceBundle.getString("SelectAll.textExtra"))
                .setPopupMenuContentModel(getPopupMenuContentModel(PopupKind.SIMPLE))
                .build();
        this.popupScrollableCommand = Command.builder()
                .setText(resourceBundle.getString("SelectAll.text"))
                .setIconFactory(Edit_paste.factory())
                .setExtraText(resourceBundle.getString("SelectAll.textExtra"))
                .setPopupMenuContentModel(getPopupMenuContentModel(PopupKind.SCROLLABLE))
                .build();
        this.popupComplexCommand = Command.builder()
                .setText(resourceBundle.getString("SelectAll.text"))
                .setIconFactory(Edit_paste.factory())
                .setExtraText(resourceBundle.getString("SelectAll.textExtra"))
                .setPopupMenuContentModel(getPopupMenuContentModel(PopupKind.COMPLEX))
                .build();

        this.add(getButtonPanel(), BorderLayout.CENTER);
    }

    private CommandPopupMenuContentModel getPopupMenuContentModel(PopupKind popupKind) {
        MessageFormat mf = new MessageFormat(resourceBundle.getString("TestMenuItem.text"));
        mf.setLocale(currLocale);

        switch (popupKind) {
            case SIMPLE:
                List<Command> simpleEntries1 = new ArrayList<>();
                List<Command> simpleEntries2 = new ArrayList<>();

                simpleEntries1.add(Command.builder()
                        .setText(mf.format(new Object[] { "1" }))
                        .setIcon(new Address_book_new()).build());
                simpleEntries1.add(Command.builder()
                        .setText(mf.format(new Object[] { "2" }))
                        .setIcon(new EmptyResizableIcon(16)).build());
                simpleEntries1.add(Command.builder()
                        .setText(mf.format(new Object[] { "3" }))
                        .setIcon(new EmptyResizableIcon(16)).build());

                simpleEntries2.add(Command.builder()
                        .setText(mf.format(new Object[] { "4" }))
                        .setIcon(new EmptyResizableIcon(16)).build());
                simpleEntries2.add(Command.builder()
                        .setText(mf.format(new Object[] { "5" }))
                        .setIcon(new Text_x_generic()).build());

                return new CommandPopupMenuContentModel(
                        Arrays.asList(new CommandGroupModel(simpleEntries1),
                                new CommandGroupModel(simpleEntries2)));

            case SCROLLABLE:
                List<Command> scrollableEntries = new ArrayList<>();

                for (int i = 0; i < 20; i++) {
                    final int index = i;
                    scrollableEntries.add(
                            Command.builder()
                                    .setText(mf.format(new Object[] { i }))
                                    .setIcon(new Text_x_generic())
                                    .setAction((CommandActionEvent e) -> System.out
                                            .println("Invoked action on '" + index + "'"))
                                    .build());
                }

                return new CommandPopupMenuContentModel(new CommandGroupModel(scrollableEntries));

            default:
                List<CommandGroupModel> extraEntries = new ArrayList<>();
                extraEntries.add(new CommandGroupModel(
                        Command.builder()
                                .setText(resourceBundle.getString("SaveSelection.text"))
                                .setIcon(new X_office_document())
                                .build(),
                        Command.builder()
                                .setText(resourceBundle.getString("ClearSelection.text"))
                                .setIcon(new EmptyResizableIcon(16))
                                .build()
                ));
                extraEntries.add(new CommandGroupModel(
                        Command.builder()
                                .setText(resourceBundle.getString("ApplyStyles.text"))
                                .setIcon(new EmptyResizableIcon(16))
                                .build()
                ));

                return new CommandPopupMenuContentModel(
                        QuickStylesPanel.getQuickStylesContentModel(resourceBundle, currLocale),
                        extraEntries);
        }
    }


    private CommandPopupMenuPresentationModel getPopupMenuPresentationModel(PopupKind popupKind) {
        switch (popupKind) {
            case SIMPLE:
                return CommandPopupMenuPresentationModel.builder().build();

            case SCROLLABLE:
                return CommandPopupMenuPresentationModel.builder()
                        .setMaxVisibleMenuCommands(8).build();

            default:
                return CommandPopupMenuPresentationModel.builder()
                        .setPanelPresentationModel(
                                CommandPanelPresentationModel.builder()
                                        .setToShowGroupLabels(false)
                                        .setCommandPresentationState(
                                                CommandButtonPresentationState.FIT_TO_ICON)
                                        .setCommandIconDimension(48)
                                        .setMaxColumns(4)
                                        .setMaxRows(3).build())
                        .build();
        }
    }

    private JPanel getButtonPanel() {
        FormBuilder builder = FormBuilder.create().
                columns("center:pref, 4dlu, center:pref, 4dlu, center:pref").
                rows("p, $lg, p, $lg, p, $lg, p, $lg, p").
                padding(Paddings.DIALOG);

        addButtons(builder, CommandButtonPresentationState.BIG, 1);
        addButtons(builder, CommandButtonPresentationState.TILE, 3);
        addButtons(builder, CommandButtonPresentationState.MEDIUM, 5);
        addButtons(builder, CommandButtonPresentationState.SMALL, 7);

        return builder.build();
    }

    private void addButtons(FormBuilder builder, CommandButtonPresentationState state, int row) {
        builder.add(createSimplePopupButton(state)).xy(1, row);
        builder.add(createScrollablePopupButton(state)).xy(3, row);
        builder.add(createComplexPopupButton(state)).xy(5, row);
    }

    private AbstractCommandButton createSimplePopupButton(CommandButtonPresentationState state) {
        return this.popupSimpleCommand.project(
                CommandPresentation.builder()
                        .setPresentationState(state)
                        .setFlat(false)
                        .setPopupMenuPresentationModel(
                                getPopupMenuPresentationModel(PopupKind.SIMPLE))
                        .build())
                .buildComponent();
    }

    private AbstractCommandButton createScrollablePopupButton(CommandButtonPresentationState state) {
        return this.popupScrollableCommand.project(
                CommandPresentation.builder()
                        .setPresentationState(state)
                        .setFlat(false)
                        .setPopupMenuPresentationModel(
                                getPopupMenuPresentationModel(PopupKind.SCROLLABLE))
                        .build())
                .buildComponent();
    }

    private AbstractCommandButton createComplexPopupButton(CommandButtonPresentationState state) {
        return this.popupComplexCommand.project(
                CommandPresentation.builder()
                        .setPresentationState(state)
                        .setFlat(false)
                        .setPopupMenuPresentationModel(
                                getPopupMenuPresentationModel(PopupKind.COMPLEX))
                        .build())
                .buildComponent();
    }

    /**
     * Main method for testing.
     *
     * @param args Ignored.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame.setDefaultLookAndFeelDecorated(true);
            SubstanceCortex.GlobalScope.setSkin(new BusinessSkin());
            TestPopupCommandButtons frame = new TestPopupCommandButtons();
            frame.setSize(800, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        });
    }
}
