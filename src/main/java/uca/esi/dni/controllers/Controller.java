package uca.esi.dni.controllers;

import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import uca.esi.dni.DniParser;
import uca.esi.dni.data.Student;
import uca.esi.dni.file.DatabaseHandler;
import uca.esi.dni.file.UtilParser;
import uca.esi.dni.models.AppModel;
import uca.esi.dni.views.EditView;
import uca.esi.dni.views.MainView;
import uca.esi.dni.views.StatsView;
import uca.esi.dni.views.View;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public abstract class Controller {

    public enum VIEW_STATES {
        main,
        edit,
        stats
    }

    protected static DatabaseHandler dbHandler = new DatabaseHandler();
    protected final AppModel model;
    protected final View view;
    protected final DniParser parent;

    public volatile boolean closedContextMenu = false;


    public Controller(DniParser parent, AppModel model, View view) {
        this.parent = parent;
        this.model = model;
        this.view = view;
    }

    public boolean isClosedContextMenu() {
        return closedContextMenu;
    }

    public void setClosedContextMenu(boolean closedContextMenu) {
        this.closedContextMenu = closedContextMenu;
    }

    public static DatabaseHandler getDbHandler() {
        return dbHandler;
    }

    public abstract void controllerLogic();

    public abstract void handleMouseEvent(MouseEvent e);

    public abstract void handleKeyEvent(KeyEvent e);

    public abstract void onContextMenuClosed(File file);

    public void changeState(VIEW_STATES state) {
        switch (state) {
            case edit:
                DniParser.currentView = new EditView(parent);
                DniParser.currentController = new EditController(parent, DniParser.appModel, DniParser.currentView);
                break;
            case stats:
                DniParser.currentView = new StatsView(parent);
                DniParser.currentController = new StatsController(parent, DniParser.appModel, DniParser.currentView);
                break;
            case main:
                DniParser.currentView = new MainView(parent);
                DniParser.currentController = new MainController(parent, DniParser.appModel, DniParser.currentView);
                break;
        }
        loadInitialState();
    }

    protected void asyncLoadStudentDataFromDB() {

        Runnable runnable = () -> {
            try {
                String idsURL = dbHandler.generateDatabaseDirectoryURL(model.getDBReference(), "Ids");
                JSONObject studentKeys = JSONObject.parse(dbHandler.getDataFromDB(idsURL));
                String emailsURL = dbHandler.generateDatabaseDirectoryURL(model.getDBReference(), "Emails");
                JSONObject studentEmails = JSONObject.parse(dbHandler.getDataFromDB(emailsURL));
                Set<Student> studentsInDB = UtilParser.generateStudentListFromJSONObject(studentKeys, studentEmails);

                model.addDBStudentList(studentsInDB);
                controllerLogic();
            } catch (IOException | NullPointerException e) {
                System.err.println("[Error loading data from DB]: " + e.getMessage());
                //Add warning
            } catch (RuntimeException e) {
                System.err.println("[Error generating student list]: " + e.getMessage());
            }

        };

        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void loadInitialState() {
        DniParser.currentController.controllerLogic();
    }

}
