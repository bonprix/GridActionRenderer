package org.vaadin.anna.gridactionrenderer.demo;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.anna.gridactionrenderer.ActionGrid;
import org.vaadin.anna.gridactionrenderer.GridAction;
import org.vaadin.anna.gridactionrenderer.GridActionRenderer.GridActionClickEvent;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class MyActionGrid extends ActionGrid<Pojo> {

    public MyActionGrid() {
        super(MyActionGrid.createActions(), new ListDataProvider<>(MyActionGrid.genPojos(10)));

        addStyleName("demogrid");
        setCaption("Action Grid");

        addColumn(Pojo::getName).setCaption("Name")
            .setId("name");

        addColumn(Pojo::getActions).setCaption("Actions")
            .setId("actions")
            .setRenderer(getGridActionRenderer());
    }

    private static List<Pojo> genPojos(final int quantity) {
        final List<Pojo> result = new ArrayList<>();

        for (long x = 1; x <= quantity; x++) {
            final Pojo pojo = new Pojo();
            pojo.setName("Item" + x);
            pojo.setActions(x % 3 == 0 ? "1,2" : "-1");
            result.add(pojo);
        }

        return result;
    }

    private static List<GridAction> createActions() {
        final List<GridAction> actions = new ArrayList<GridAction>();
        actions.add(new GridAction(FontAwesome.USER, "user"));
        actions.add(new GridAction(FontAwesome.GEAR, "settings"));
        return actions;
    }

    @Override
    public void click(final GridActionClickEvent<Pojo> event) {
        Notification.show(event.getBean()
            .getName() + " - "
                + event.getAction()
                    .getDescription(), Type.ERROR_MESSAGE);
    }

}
