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
package org.pushingpixels.radiance.component.internal.ui.ribbon;

import org.pushingpixels.radiance.component.api.common.CommandButtonPresentationState;
import org.pushingpixels.radiance.component.api.common.JCommandButton;
import org.pushingpixels.radiance.component.api.common.model.Command;
import org.pushingpixels.radiance.component.api.common.model.CommandButtonPresentationModel;
import org.pushingpixels.radiance.component.api.common.model.CommandGroup;
import org.pushingpixels.radiance.component.api.common.model.CommandStripPresentationModel;
import org.pushingpixels.radiance.component.api.common.model.CommandStripPresentationModel.StripOrientation;
import org.pushingpixels.radiance.component.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.radiance.component.api.common.popup.PopupPanelManager;
import org.pushingpixels.radiance.component.api.common.projection.CommandStripProjection;
import org.pushingpixels.radiance.component.api.common.projection.Projection;
import org.pushingpixels.radiance.component.api.ribbon.JRibbonBand;
import org.pushingpixels.radiance.component.api.ribbon.model.RibbonGalleryContentModel;
import org.pushingpixels.radiance.component.internal.utils.ComponentUtilities;
import org.pushingpixels.radiance.component.internal.utils.KeyTipManager;
import org.pushingpixels.radiance.theming.api.RadianceThemingCortex;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic UI for ribbon gallery {@link JRibbonGallery}.
 *
 * @author Kirill Grouchnikov
 */
public abstract class BasicRibbonGalleryUI extends RibbonGalleryUI {
    /**
     * The associated ribbon gallery.
     */
    protected JRibbonGallery ribbonGallery;

    /**
     * The index of the first visible button.
     */
    private int firstVisibleButtonIndex;

    /**
     * The count of visible buttons.
     */
    private int visibleButtonsInEachRow;

    private int visibleButtonRowNumber;

    private Command scrollDownCommand;
    private Command scrollUpCommand;
    private Command expandCommand;

    private CommandGroup galleryScrollerCommands;

    /**
     * Contains the scroll down, scroll up and show popup buttons.
     */
    private JComponent buttonStrip;

    private RibbonGalleryContentModel.GalleryCommandAction
            galleryCommandSelectionListener;
    private ChangeListener galleryModelChangeListener;

    /**
     * Ribbon gallery margin.
     */
    protected Insets margin;

    @Override
    public void installUI(JComponent c) {
        this.ribbonGallery = (JRibbonGallery) c;
        this.firstVisibleButtonIndex = 0;

        this.installDefaults();
        this.installComponents();
        this.installListeners();

        c.setLayout(createLayoutManager());
    }

    /**
     * Installs subcomponents on the associated ribbon gallery.
     */
    @SuppressWarnings("unchecked")
    protected void installComponents() {
        // Gallery scroller commands
        this.scrollUpCommand = Command.builder()
                .setAction(commandActionEvent -> {
                    scrollOneRowUp();
                    ribbonGallery.revalidate();
                })
                .build();
        this.scrollDownCommand = Command.builder()
                .setAction(commandActionEvent -> {
                    scrollOneRowDown();
                    ribbonGallery.revalidate();
                })
                .build();
        this.expandCommand = Command.builder()
                .setAction(commandActionEvent -> {
                    PopupPanelManager.defaultManager().hidePopups(ribbonGallery);
                    SwingUtilities.invokeLater(() -> {
                        PopupFactory popupFactory = PopupFactory.getSharedInstance();

                        JCommandPopupMenu popupMenu = JRibbonGallery.getExpandPopupMenu(
                                ribbonGallery.getProjection(),
                                ribbonGallery.getComponentOrientation()).buildComponent();
                        final Point loc = ribbonGallery.getLocationOnScreen();
                        popupMenu.setCustomizer(() -> {
                            Rectangle scrBounds =
                                    ribbonGallery.getGraphicsConfiguration().getBounds();

                            boolean ltr = popupMenu.getComponentOrientation().isLeftToRight();

                            Dimension pref = popupMenu.getPreferredSize();
                            int width = Math.max(pref.width, ribbonGallery.getWidth());
                            int height = pref.height;

                            int x = ltr ? loc.x : loc.x + width - pref.width;
                            int y = loc.y;

                            // make sure that the popup stays in bounds
                            if ((x + width) > (scrBounds.x + scrBounds.width)) {
                                x = scrBounds.x + scrBounds.width - width;
                            }
                            if ((y + height) > (scrBounds.y + scrBounds.height)) {
                                y = scrBounds.y + scrBounds.height - height;
                            }

                            return new Rectangle(x, y, width, height);
                        });

                        // get the popup and show it
                        Dimension pref = popupMenu.getPreferredSize();
                        int width = Math.max(pref.width, ribbonGallery.getWidth());

                        boolean ltr = ribbonGallery.getComponentOrientation().isLeftToRight();
                        int x = ltr ? loc.x : loc.x + ribbonGallery.getWidth() - width;
                        Popup popup = popupFactory.getPopup(ribbonGallery, popupMenu, x, loc.y);
                        PopupPanelManager.defaultManager().addPopup(ribbonGallery, popup,
                                popupMenu);
                    });
                })
                .build();

        // Configure the overlay for the expand command to show the gallery's expand key tip
        Map<Command, CommandButtonPresentationModel.Overlay> galleryScrollerOverlays =
                new HashMap<>();
        galleryScrollerOverlays.put(this.scrollUpCommand,
                CommandButtonPresentationModel.overlay()
                        .setAutoRepeatAction(true)
                        .setAutoRepeatActionIntervals(200, 50));
        galleryScrollerOverlays.put(this.scrollDownCommand,
                CommandButtonPresentationModel.overlay()
                        .setAutoRepeatAction(true)
                        .setAutoRepeatActionIntervals(200, 50));
        galleryScrollerOverlays.put(this.expandCommand,
                CommandButtonPresentationModel.overlay()
                        .setActionKeyTip(this.ribbonGallery.getProjection()
                                .getPresentationModel().getExpandKeyTip())
                        .setFireActionTrigger(CommandButtonPresentationModel.FireActionTrigger.ON_PRESSED));

        // Configure customizers for all the scroller buttons (setting icons and additional
        // straight sides)
        Map<Command, Projection.ComponentCustomizer<JCommandButton>> galleryScrollerCustomizers = new HashMap<>();
        galleryScrollerCustomizers.put(this.scrollUpCommand, this::configureScrollUpButton);
        galleryScrollerCustomizers.put(this.scrollDownCommand, this::configureScrollDownButton);
        galleryScrollerCustomizers.put(this.expandCommand, this::configureExpandButton);

        // Configure the component supplier for the expand command to return our own subclass
        Map<Command, Projection.ComponentSupplier<JCommandButton, Command,
                CommandButtonPresentationModel>> galleryScrollerSuppliers = new HashMap<>();
        galleryScrollerSuppliers.put(this.expandCommand, projection -> ExpandCommandButton::new);

        // Create a button strip that hosts all three scroller commands with all the additional
        // command-specific configurations
        this.galleryScrollerCommands = new CommandGroup(this.scrollUpCommand,
                this.scrollDownCommand, this.expandCommand);
        CommandStripProjection projection = new CommandStripProjection(
                this.galleryScrollerCommands,
                CommandStripPresentationModel.builder()
                        .setOrientation(StripOrientation.VERTICAL)
                        .setCommandPresentationState(CommandButtonPresentationState.FIT_TO_ICON)
                        .setFlat(false)
                        .setToDismissPopupsOnActivation(false)
                        .build());
        projection.setCommandComponentSuppliers(galleryScrollerSuppliers);
        projection.setCommandComponentCustomizers(galleryScrollerCustomizers);
        projection.setCommandOverlays(galleryScrollerOverlays);

        this.buttonStrip = projection.buildComponent();

        this.ribbonGallery.add(this.buttonStrip);
    }

    /**
     * Uninstalls subcomponents from the associated ribbon gallery.
     */
    protected void uninstallComponents() {
        this.galleryScrollerCommands.removeAllCommands();
        this.buttonStrip.removeAll();
        this.ribbonGallery.remove(this.buttonStrip);
    }

    /**
     * Installs defaults on the associated ribbon gallery.
     */
    protected void installDefaults() {
        this.margin = new Insets(3, 3, 3, 3);
        Border b = this.ribbonGallery.getBorder();
        if (b == null || b instanceof UIResource) {
            this.ribbonGallery.setBorder(new BorderUIResource.EmptyBorderUIResource(2, 2, 2, 2));
        }
        this.ribbonGallery.setOpaque(false);
    }

    /**
     * Uninstalls defaults from the associated ribbon gallery.
     */
    protected void uninstallDefaults() {
    }

    /**
     * Installs listeners on the associated ribbon gallery.
     */
    protected void installListeners() {
        this.galleryCommandSelectionListener = (Command activated) ->
                SwingUtilities.invokeLater(() -> {
                    if (ribbonGallery != null) {
                        scrollToSelected();
                        ribbonGallery.revalidate();
                    }
                });
        this.ribbonGallery.getProjection().getContentModel().addCommandActivationListener(
                this.galleryCommandSelectionListener);

        this.galleryModelChangeListener = (ChangeEvent changeEvent) -> ribbonGallery.revalidate();
        this.ribbonGallery.getProjection().getContentModel().addChangeListener(
                this.galleryModelChangeListener);
    }

    /**
     * Uninstalls listeners from the associated ribbon gallery.
     */
    protected void uninstallListeners() {
        this.ribbonGallery.getProjection().getContentModel().removeCommandActivationListener(
                this.galleryCommandSelectionListener);
        this.ribbonGallery.getProjection().getContentModel().removeChangeListener(
                this.galleryModelChangeListener);
    }

    @Override
    public void uninstallUI(JComponent c) {
        c.setLayout(null);

        this.uninstallListeners();
        this.uninstallDefaults();
        this.uninstallComponents();

        this.ribbonGallery = null;
    }

    /**
     * Invoked by <code>installUI</code> to create a layout manager object to manage the
     * ribbon gallery.
     *
     * @return a layout manager object
     */
    protected LayoutManager createLayoutManager() {
        return new RibbonGalleryLayout();
    }

    /**
     * Layout for the ribbon gallery.
     *
     * @author Kirill Grouchnikov
     */
    private class RibbonGalleryLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component c) {
        }

        @Override
        public void removeLayoutComponent(Component c) {
        }

        @Override
        public Dimension preferredLayoutSize(Container c) {
            return new Dimension(
                    ribbonGallery.getPreferredWidth(ribbonGallery.getPresentationPriority(),
                            c.getHeight()), c.getHeight());
        }

        @Override
        public Dimension minimumLayoutSize(Container c) {
            return this.preferredLayoutSize(c);
        }

        @Override
        public void layoutContainer(Container c) {
            int width = c.getWidth();
            int height = c.getHeight();

            Insets borderInsets = ribbonGallery.getInsets();

            int galleryHeight = height - margin.top - margin.bottom;
            int buttonHeight = galleryHeight - borderInsets.top - borderInsets.bottom;

            visibleButtonRowNumber = 1;
            CommandButtonPresentationState galleryButtonPresentationState = ribbonGallery
                    .getProjection().getPresentationModel().getCommandPresentationState();
            if (galleryButtonPresentationState == CommandButtonPresentationState.SMALL) {
                buttonHeight /= 3;
                visibleButtonRowNumber = 3;
            }

            boolean ltr = c.getComponentOrientation().isLeftToRight();
            int scrollerButtonHeight = galleryHeight / 3;
            int scrollerButtonWidth = getScrollerButtonWidth();
            int buttonX = ltr ? width - scrollerButtonWidth - margin.right : margin.left;

            buttonStrip.getComponent(0).setPreferredSize(
                    new Dimension(scrollerButtonWidth, scrollerButtonHeight));
            buttonStrip.getComponent(1).setPreferredSize(
                    new Dimension(scrollerButtonWidth, scrollerButtonHeight));
            // special case (if available height doesn't divide 3)
            buttonStrip.getComponent(2).setPreferredSize(
                    new Dimension(scrollerButtonWidth, galleryHeight - 2 * scrollerButtonHeight));
            buttonStrip.setBounds(buttonX, margin.top, scrollerButtonWidth, galleryHeight);
            buttonStrip.doLayout();

            // hide all buttons and compute the button width
            int maxButtonWidth = buttonHeight;
            if (galleryButtonPresentationState == JRibbonBand.BIG_FIXED_LANDSCAPE) {
                maxButtonWidth = maxButtonWidth * 5 / 4;
            }

            int gap = getLayoutGap();

            // compute how many buttons can fit in each row
            visibleButtonsInEachRow = 0;
            int availableButtonsSpace = ltr ? buttonX - margin.left
                    : width - buttonX - scrollerButtonWidth - margin.right;
            while (true) {
                // gap on the left, gap in between every two adjacent
                // buttons and gap on the right
                int neededSpace = visibleButtonsInEachRow * maxButtonWidth
                        + (visibleButtonsInEachRow + 1) * gap;
                if (neededSpace > availableButtonsSpace) {
                    visibleButtonsInEachRow--;
                    break;
                }
                visibleButtonsInEachRow++;
            }

            // System.out.println("Visible in each row " +
            // visibleButtonsInEachRow);

            // compute how many pixels we can distribute among the visible buttons
            int neededSpace = visibleButtonsInEachRow * maxButtonWidth
                    + (visibleButtonsInEachRow + 1) * gap;
            int startX = ltr ? margin.left + gap : width - margin.right - gap;
            int availableWidth = ltr ? buttonX - margin.right
                    : width - buttonX - scrollerButtonWidth - margin.left;
            int toAddToButtonWidth = (availableWidth - neededSpace) / visibleButtonsInEachRow;

            // compute how many buttons can fit in the available horizontal space
            int lastVisibleButtonIndex = firstVisibleButtonIndex
                    + visibleButtonRowNumber * visibleButtonsInEachRow - 1;
            lastVisibleButtonIndex = Math.min(lastVisibleButtonIndex,
                    ribbonGallery.getCommandCount() - 1);
            int currCountInRow = 0;
            int buttonY = margin.top + borderInsets.top;
            int singleButtonWidth = maxButtonWidth + toAddToButtonWidth;
            // Mark the buttons before the first visible and after the last visible as not visible
            // Note that toggling visibility to false and then true messes up with focus traversal,
            // which is why there's only one call to setVisible for every button in the gallery.
            for (int i = 0; i < firstVisibleButtonIndex; i++) {
                ribbonGallery.getButtonAt(i).setVisible(false);
            }
            if (lastVisibleButtonIndex >= 0) {
                for (int i = lastVisibleButtonIndex + 1; i < ribbonGallery.getCommandCount(); i++) {
                    ribbonGallery.getButtonAt(i).setVisible(false);
                }
            }
            for (int i = firstVisibleButtonIndex; i <= lastVisibleButtonIndex; i++) {
                JCommandButton currButton = ribbonGallery.getButtonAt(i);

                // show button and set bounds
                currButton.setVisible(true);
                if (ltr) {
                    currButton.setBounds(startX, buttonY, singleButtonWidth, buttonHeight);
                    startX += (singleButtonWidth + gap);
                } else {
                    currButton.setBounds(startX - singleButtonWidth, buttonY, singleButtonWidth,
                            buttonHeight);
                    startX -= (singleButtonWidth + gap);
                }
                currCountInRow++;
                if (currCountInRow == visibleButtonsInEachRow) {
                    currCountInRow = 0;
                    if (ltr) {
                        startX = margin.left + gap;
                    } else {
                        startX = width - margin.right - gap;
                    }
                    buttonY += buttonHeight;
                }
            }
            if (ribbonGallery.getCommandCount() == 0) {
                scrollDownCommand.setActionEnabled(false);
                scrollUpCommand.setActionEnabled(false);
                expandCommand.setActionEnabled(false);
            } else {
                // Scroll down command is enabled when the last button is not showing
                scrollDownCommand.setActionEnabled(!ribbonGallery
                        .getButtonAt(ribbonGallery.getCommandCount() - 1).isVisible());
                // Scroll up command is enabled when the first button is not showing
                scrollUpCommand.setActionEnabled(!ribbonGallery.getButtonAt(0).isVisible());
                expandCommand.setActionEnabled(true);
            }
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        this.paintRibbonGalleryBorder(graphics);

        graphics.dispose();
    }

    /**
     * Paints ribbon gallery border.
     *
     * @param graphics Graphics context.
     */
    protected abstract void paintRibbonGalleryBorder(Graphics graphics);

    /**
     * Returns the layout gap for the controls in the associated ribbon gallery.
     *
     * @return The layout gap for the controls in the associated ribbon gallery.
     */
    protected int getLayoutGap() {
        return 4;
    }

    /**
     * Returns the preferred width of the ribbon gallery for the specified parameters.
     *
     * @param buttonCount     Button count.
     * @param availableHeight Available height in pixels.
     * @return The preferred width of the ribbon gallery for the specified parameters.
     */
    public int getPreferredWidth(int buttonCount, int availableHeight) {
        Insets borderInsets = ribbonGallery.getInsets();

        int galleryHeight = availableHeight - margin.top - margin.bottom;
        int buttonHeight = galleryHeight - borderInsets.top - borderInsets.bottom;

        // start at the left margin
        int result = margin.left;
        // add all the gallery buttons - based on the presentation state
        CommandButtonPresentationState galleryButtonPresentationState = ribbonGallery
                .getProjection().getPresentationModel().getCommandPresentationState();
        if (galleryButtonPresentationState == CommandButtonPresentationState.SMALL) {
            result += buttonCount * buttonHeight / 3;
        }
        if (galleryButtonPresentationState == JRibbonBand.BIG_FIXED) {
            result += buttonCount * buttonHeight;
        }
        if (galleryButtonPresentationState == JRibbonBand.BIG_FIXED_LANDSCAPE) {
            result += buttonCount * buttonHeight * 5 / 4;
        }
        // and the gaps between them (including before first and after last)
        result += (buttonCount + 1) * getLayoutGap();
        // and the control button strip width
        result += getScrollerButtonWidth();
        // and the gap to the right margin
        result += margin.right;

        // System.out.println(buttonCount + "/" + availableHeight + "/"
        // + buttonHeight + " --> " + result);
        return result;
    }

    private int getScrollerButtonWidth() {
        return ComponentUtilities.getScaledSize(15, RadianceThemingCortex.GlobalScope.getFontPolicy()
                .getFontSet().getControlFont().getSize(), 1.0f, 1);
    }

    /**
     * Scrolls the contents of this ribbon gallery one row down.
     */
    private void scrollOneRowDown() {
        this.firstVisibleButtonIndex += this.visibleButtonsInEachRow;
    }

    /**
     * Scrolls the contents of this ribbon gallery one row up.
     */
    private void scrollOneRowUp() {
        this.firstVisibleButtonIndex -= this.visibleButtonsInEachRow;
    }

    /**
     * Scrolls the contents of this ribbon gallery to reveal the currently selected button.
     */
    private void scrollToSelected() {
        JCommandButton selected = this.ribbonGallery.getSelectedButton();
        if (selected == null) {
            return;
        }
        int selIndex = -1;
        for (int i = 0; i < this.ribbonGallery.getCommandCount(); i++) {
            if (this.ribbonGallery.getButtonAt(i) == selected) {
                selIndex = i;
                break;
            }
        }
        if (selIndex < 0) {
            return;
        }

        // is already shown?
        if ((selIndex >= this.firstVisibleButtonIndex) && (selIndex < (this.firstVisibleButtonIndex
                + this.visibleButtonRowNumber * this.visibleButtonsInEachRow))) {
            return;
        }

        // not visible?
        if (this.visibleButtonsInEachRow <= 0) {
            return;
        }

        while (true) {
            if (selIndex < this.firstVisibleButtonIndex) {
                // need to scroll up
                this.scrollOneRowUp();
            } else {
                this.scrollOneRowDown();
            }
            if ((selIndex >= this.firstVisibleButtonIndex)
                    && (selIndex < (this.firstVisibleButtonIndex
                    + this.visibleButtonRowNumber * this.visibleButtonsInEachRow))) {
                return;
            }
        }
    }

    @KeyTipManager.HasNextKeyTipChain
    private static class ExpandCommandButton extends JCommandButton {
        public ExpandCommandButton(Projection<JCommandButton, Command,
                CommandButtonPresentationModel> projection) {
            super(projection);
        }
    }

    protected abstract void configureScrollUpButton(JCommandButton button);

    protected abstract void configureScrollDownButton(JCommandButton button);

    protected abstract void configureExpandButton(JCommandButton button);
}
