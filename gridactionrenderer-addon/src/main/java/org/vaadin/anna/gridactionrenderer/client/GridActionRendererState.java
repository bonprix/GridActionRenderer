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
import java.util.List;

import com.vaadin.shared.ui.grid.renderers.AbstractRendererState;

/**
 * Shared state class for GridActionRenderer.
 */
public class GridActionRendererState extends AbstractRendererState {

    public List<GridActionRendererState.GridAction> gridActions = new ArrayList<GridActionRendererState.GridAction>();

    /**
     * Shared state representation of org.vaadin.anna.gridactionrenderer.GridAction class.
     */
    public static class GridAction implements java.io.Serializable {
        public String actionKey;
        public String description;
        public final List<String> styleNames = new ArrayList<String>();
    }
}