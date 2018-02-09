/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.anna.gridactionrenderer.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.vaadin.anna.gridactionrenderer.client.GridActionRenderer.GridActionPanel;
import org.vaadin.anna.gridactionrenderer.client.GridActionRendererState.GridAction;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.renderers.WidgetRenderer;
import com.vaadin.client.ui.Icon;
import com.vaadin.client.widget.grid.RendererCellReference;

/**
 * Widget class for rendering a list of {@link GridAction}s.
 */
public class GridActionRenderer extends WidgetRenderer<String, GridActionPanel> {
    private final Logger logger = Logger.getLogger(GridActionRenderer.class.getSimpleName());

    private final GridActionRendererConnector connector;

    /**
     * Constructor. Connector reference is required for accessing state before state change methods are triggered.
     *
     * @param connector the connector
     */
    public GridActionRenderer(final GridActionRendererConnector connector) {
        super();
        this.connector = connector;
    }

    @Override
    public GridActionPanel createWidget() {
        return new GridActionPanel();
    }

    @Override
    public void render(final RendererCellReference cell, final String data, final GridActionPanel widget) {
        widget.setCellCoordinates(cell.getColumnIndex(), cell.getRowIndex());
        widget.setActionVisibility(data);
    }

    /**
     * Fetches actions directly from the state, because state change methods are triggered too late for the purpose of this method.
     *
     * @return actions to be added to the widget
     */
    private List<GridAction> getGridActions() {
        return ((GridActionRendererState) this.connector.getState()).gridActions;
    }

    /**
     * A panel widget for displaying GridAction icons in a row.
     */
    public class GridActionPanel extends FlowPanel {

        private int columnIndex;
        private int rowIndex;
        private final List<Widget> widgets = new LinkedList<>();

        public GridActionPanel() {
            super();
            setStylePrimaryName("grid-action-panel");

            final List<GridAction> gridActions = getGridActions();
            for (final GridAction gridAction : gridActions) {

                final GridActionWidget actionWidget = new GridActionWidget(gridAction);
                this.widgets.add(actionWidget);

                // special tooltip handling, won't work if the used Grid
                // implementation doesn't support custom tooltips
                GridActionRenderer.this.connector.getConnection()
                    .getVTooltip()
                    .connectHandlersToWidget(actionWidget);

                // click handling
                actionWidget.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(final ClickEvent event) {
                        GridActionRenderer.this.connector.handleClick(event, gridAction, GridActionPanel.this.columnIndex, GridActionPanel.this.rowIndex);
                    }
                });
            }
        }

        public void setCellCoordinates(final int columnIndex, final int rowIndex) {
            this.columnIndex = columnIndex;
            this.rowIndex = rowIndex;
        }

        public void setActionVisibility(final String data) {
            clear();
            final List<Integer> visibleActionIndexes = new ArrayList<Integer>();
            if (data != null && data.length() > 0) {
                final String[] parts = data.split(",");
                for (final String part : parts) {
                    try {
                        visibleActionIndexes.add(Integer.valueOf(part.trim()));
                    }
                    catch (final NumberFormatException e) {
                        GridActionRenderer.this.logger.log(Level.SEVERE, "Can't parse \"" + part + "\" into an Integer," + " action visibility check failed.",
                                                           e);
                    }
                }
            }
            int actionCount = 0;
            for (final Widget child : this.widgets) { // getChildren()) {
                if (child instanceof GridActionWidget) {
                    // update the visibility of all GridActionWidgets
                    final boolean visible = visibleActionIndexes.contains(-1) || visibleActionIndexes.contains(actionCount);
                    if (visible) {
                        add(child);
                    }
                    ++actionCount;
                }
            }
        }
    }

    /**
     * Widget that displays a {@link GridAction} as a clickable icon. If the GridAction has a description and the used Grid supports custom tooltips, the
     * description is displayed as a tooltip when hovering. If the action has custom styles, those are added to the parent element of this widget.
     */
    public class GridActionWidget extends Composite {

        public GridActionWidget(final GridAction gridAction) {
            final boolean hasDescription = gridAction.description != null && !gridAction.description.isEmpty();

            final Icon icon = GridActionRenderer.this.connector.getConnection()
                .getIcon(GridActionRenderer.this.connector.getResourceUrl(gridAction.actionKey));
            if (icon != null) {
                icon.setAlternateText("icon");
                icon.addStyleName("grid-action-widget-icon");
                if (hasDescription) {
                    icon.getElement()
                        .setAttribute(GridActionRendererConnector.TOOLTIP, gridAction.description);
                }
            }

            final Button button = new Button();
            button.setStyleName("grid-action-widget");

            for (final String styleName : gridAction.styleNames) {
                button.addStyleName(styleName);
            }

            if (icon != null) {
                button.getElement()
                    .appendChild(icon.getElement());
            }
            if (hasDescription) {
                button.getElement()
                    .setAttribute(GridActionRendererConnector.TOOLTIP, gridAction.description);
            }

            initWidget(button);
        }

        /**
         * Adds click handler for the action widget and sinks the native event.
         *
         * @param handler the handler
         * @return {@link HandlerRegistration} used to remove the handler
         */
        public HandlerRegistration addClickHandler(final ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }
    }
}
