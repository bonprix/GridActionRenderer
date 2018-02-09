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
package org.vaadin.anna.gridactionrenderer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.anna.gridactionrenderer.client.GridActionClickRpc;
import org.vaadin.anna.gridactionrenderer.client.GridActionRendererState;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.ConnectorEventListener;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.server.KeyMapper;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.AbstractRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.util.ReflectTools;

/**
 * Renderer for displaying a row of {@link GridAction} icons. The value of the column must be a String that contains indexes of the visible action icons,
 * separated by comma, or -1 if all actions should be displayed. Order of the indexes does not affect the order of the displayed actions, which is determined by
 * the list given as a constructor parameter.
 */
public class GridActionRenderer<BEANTYPE> extends AbstractRenderer<Grid<BEANTYPE>, String> {

    private KeyMapper<GridAction> actionKeyMapper = null;

    /**
     * Constructor. If the contents of the actions list given as a parameter are changed after this constructor is called those changes won't be taken into
     * account. The visibility of each action can be determined by row (see {@link GridActionRenderer} for further details).
     *
     * @param actions all the actions that are to be displayed by this renderer
     */
    public GridActionRenderer(final List<GridAction> actions) {
        super(String.class);

        this.actionKeyMapper = new KeyMapper<GridAction>();
        setActions(actions);

        registerRpc(new GridActionClickRpc() {
            @Override
            public void click(final int rowIndex, final String actionKey, final MouseEventDetails mouseDetails) {
                final GridAction action = GridActionRenderer.this.actionKeyMapper.get(actionKey);

                final ListDataProvider<BEANTYPE> p = (ListDataProvider) getParentGrid().getDataProvider();
                final List<BEANTYPE> allItems = new ArrayList<BEANTYPE>(p.getItems());
                final Object item = allItems.get(rowIndex);

                fireEvent(new GridActionClickEvent(getParentGrid(), item, action, mouseDetails));
            }
        });
    }

    @Override
    protected GridActionRendererState getState() {
        return (GridActionRendererState) super.getState();
    }

    /**
     * Sets all {@link GridActions} that can be displayed by this renderer. The visibility of each action can be determined by row (see
     * {@link GridActionRenderer} for further details).
     *
     * @param gridActions list of GridActions that can be displayed
     */
    private void setActions(final List<GridAction> gridActions) {
        final ArrayList<GridActionRendererState.GridAction> stateActions = new ArrayList<GridActionRendererState.GridAction>();

        for (final GridAction gridAction : gridActions) {
            final String key = this.actionKeyMapper.key(gridAction);
            setResource(key, gridAction.getIcon());

            final GridActionRendererState.GridAction stateAction = new GridActionRendererState.GridAction();
            stateAction.actionKey = key;
            stateAction.description = gridAction.getDescription();
            for (final String styleName : gridAction.getStyleNames()) {
                stateAction.styleNames.add(styleName);
            }

            stateActions.add(stateAction);
        }

        getState().gridActions.clear();
        getState().gridActions.addAll(stateActions);
    }

    /**
     * Adds a {@link GridAction} click listener to this renderer. The listener is invoked every time one of the rendered GridAction icons are clicked.
     *
     * @param listener the click listener to be added
     */
    public void addActionClickListener(final GridActionClickListener<BEANTYPE> listener) {
        addListener(GridActionClickEvent.class, listener, GridActionClickListener.CLICK_METHOD);
    }

    /**
     * Removes the given {@link GridAction} click listener from this renderer.
     *
     * @param listener the click listener to be removed
     */
    public void removeGridActionClickListener(final GridActionClickListener<BEANTYPE> listener) {
        removeListener(GridActionClickEvent.class, listener);
    }

    /**
     * An interface for listening to {@link GridActionClickEvent GridAction click events} that occur when user clicks a corresponding action icon.
     * <p>
     * See {@link com.vaadin.ui.renderers.ClickableRenderer#addClickListener(RendererClickListener)} for more information.
     */
    public interface GridActionClickListener<BEANTYPE> extends ConnectorEventListener {

        static final Method CLICK_METHOD = ReflectTools.findMethod(GridActionClickListener.class, "click", GridActionClickEvent.class);

        /**
         * Called when a rendered grid action icon gets clicked.
         *
         * @param event data about the click
         */
        void click(GridActionClickEvent<BEANTYPE> event);
    }

    /**
     * Class for holding information about a mouse click event that triggers a {@link GridAction}. A GridActionClickEvent is fired when the user clicks on a
     * GridAction icon.
     * <p>
     * See {@link ClickEvent} for more information about the event data.
     */
    public static class GridActionClickEvent<BEANTYPE> extends ClickEvent {
        private final BEANTYPE item;
        private final GridAction action;

        protected GridActionClickEvent(final Grid source, final BEANTYPE item, final GridAction action, final MouseEventDetails mouseEventDetails) {
            super(source, mouseEventDetails);

            this.item = item;
            this.action = action;
        }

        /**
         * Returns the bean of the row where the action click event was triggered.
         *
         * @return bean for of the clicked row
         */
        public BEANTYPE getBean() {
            return this.item;
        }

        /**
         * Returns the triggered {@link GridAction}.
         *
         * @return triggered action
         */
        public GridAction getAction() {
            return this.action;
        }
    }
}
