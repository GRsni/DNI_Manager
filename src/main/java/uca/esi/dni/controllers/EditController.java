package uca.esi.dni.controllers;

import org.jetbrains.annotations.NotNull;
import processing.data.JSONObject;
import processing.data.Table;
import processing.data.TableRow;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import uca.esi.dni.DniParser;
import uca.esi.dni.data.Student;
import uca.esi.dni.file.DatabaseHandler;
import uca.esi.dni.file.EmailHandler;
import uca.esi.dni.file.UtilParser;
import uca.esi.dni.models.AppModel;
import uca.esi.dni.ui.BaseElement;
import uca.esi.dni.ui.ItemList;
import uca.esi.dni.ui.TextField;
import uca.esi.dni.views.View;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static processing.core.PConstants.ENTER;
import static processing.core.PConstants.*;
import static processing.event.KeyEvent.TYPE;
import static processing.event.MouseEvent.*;
import static uca.esi.dni.controllers.Controller.VIEW_STATES.main;

public class EditController extends Controller {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public EditController(DniParser parent, AppModel model, View view) {
        super(parent, model, view);
        controllerLogic();
    }

    @Override
    public void controllerLogic() {
        view.update(model.getDBStudents(), model.getTemporaryStudents(), model.getInputFile(), model.getDBReference());
    }

    @Override
    public void handleMouseEvent(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        switch (e.getAction()) {
            case CLICK:
                if (view.isModalActive()) {
                    if (view.getUIModalElement("confirmEmptyB").inside(x, y)) {
                        asyncEmptyDBButtonHook();
                    }
                    setModalVisibility(false);
                } else {
                    TextField idTF = (TextField) view.getUIElement("idTF");
                    TextField emailTF = (TextField) view.getUIElement("emailTF");
                    if (view.getUIElement("enterStudentB").inside(x, y)) {
                        if (checkManualStudentData(idTF, emailTF)) {
                            addManualDataToAuxList(idTF, emailTF);
                            clearTextFields(idTF, emailTF);
                        }
                    } else if (view.getUIElement("removeStudentAuxB").inside(x, y)) {
                        if (checkManualStudentData(idTF, emailTF)) {
                            removeStudentFromAuxList(idTF.getContent());
                            clearTextFields(idTF, emailTF);
                        }
                    } else if (view.getUIElement("selectFileB").inside(x, y)) {
                        selectInputFile();
                    } else if (view.getUIElement("backB").inside(x, y)) {
                        changeState(main);
                    } else if (view.getUIElement("addToListB").inside(x, y)) {
                        asyncAddStudentsToDBListButtonHook();
                    } else if (view.getUIElement("deleteFromListB").inside(x, y)) {
                        asyncRemoveStudentsFromDBButtonHook();
                    } else if (view.getUIElement("emptyListB").inside(x, y)) {
                        setModalVisibility(true);
                    } else if (idTF.inside(x, y)) {
                        idTF.setFocused(true);
                        emailTF.setFocused(false);
                    } else if (emailTF.inside(x, y)) {
                        idTF.setFocused(false);
                        emailTF.setFocused(true);
                    } else if (view.getUIElement("dbStudentIL").inside(x, y)) {
                        view.getUIElement("dbStudentsIL").handleInput(e);
                    } else if (view.getUIElement("auxStudentsIL").inside(x, y)) {
                        view.getUIElement("auxStudentsIL").handleInput(e);
                    }
                }

                for (String key : view.getElementKeys()) {
                    BaseElement element = view.getUIElement(key);
                    if (element.isClicked()) {
                        element.isClicked(false);
                    }
                }

                break;
            case MouseEvent.MOVE:
                checkHover(x, y);
                break;
            case PRESS:
                checkPress(x, y);
            case RELEASE:
                //release mouse event is unreliable, multiple events per mouse click
                break;
            case WHEEL:
                ItemList dbList = (ItemList) view.getUIElement("dbStudentIL");
                ItemList auxList = (ItemList) view.getUIElement("auxStudentsIL");
                if (dbList.inside(x, y)) {
                    dbList.handleInput(e);
                } else if (auxList.inside(x, y)) {
                    auxList.handleInput(e);
                }
                break;
        }
        controllerLogic();
    }

    @Override
    public void handleKeyEvent(KeyEvent e) {
        if (e.getAction() == TYPE) {

            final char k = e.getKey();
            TextField tf = getFocusedTextField();

            if (k == BACKSPACE) {
                tf.removeCharacter();
            } else if (k == ENTER || k == RETURN) {
                tf.setFocused(false);
            } else if (k == DELETE) {
                tf.setContent("");
            } else if (k >= ' ') {
                tf.addCharToContent(k);
            }
        }
    }

    private void setModalVisibility(boolean visibility) {
        view.setModalActive(visibility);
        for (String key : view.getModalElementKeys()) {
            view.getUIModalElement(key).setVisible(visibility);
        }
    }

    private void addManualDataToAuxList(TextField idTF, TextField emailTF) {
        try {
            model.addTemporaryStudent(new Student(idTF.getContent(), emailTF.getContent()));
            addWarning("Alumno introducido correctamente.", 250, true);
        } catch (NullPointerException e) {
            System.err.println("[Error while trying to insert new Student into temporary list]: " + e.getMessage());
            LOGGER.severe("[Error while trying to insert new Student into temporary list]: " + e.getMessage());
            addWarning("Error al introducir el alumno.", 200, false);
        }

    }

    private void clearTextFields(TextField idTF, TextField emailTF) {
        idTF.setContent("");
        emailTF.setContent("");
        getFocusedTextField().setFocused(false);
    }


    private boolean checkManualStudentData(TextField idTF, TextField emailTF) {
        if (!idTF.getContent().isEmpty()) {
            if (!emailTF.getContent().isEmpty()) {
                if (EmailHandler.isValidEmailAddress(emailTF.getContent())) {
                    return true;
                } else {
                    System.err.println("[Error validating email address].");
                    LOGGER.warning("[Error validating email address]: No valid address.");
                    addWarning("Email no válido.", 200, false);
                    return false;
                }
            } else {
                return true;
            }
        } else {
            System.err.println("[Error while reading student data]: Empty ID field");
            LOGGER.warning("[Error while reading student data]:Empty ID field.");
            addWarning("Identificador no introducido.", 200, false);
            return false;
        }
    }

    private void removeStudentFromAuxList(String id) {
        for (Student student : model.getTemporaryStudents()) {
            if (student.getID().equals(id)) {
                model.removeTemporaryStudent(student);
                addWarning("Alumno eliminado de la lista temporal.", 100, true);
                break;
            }
        }
    }


    private void asyncAddStudentsToDBListButtonHook() {
        Runnable runnable = () -> {

            Set<Student> uniqueStudentSet = UtilParser.getUniqueStudentSet(model.getTemporaryStudents(), model.getDBStudents());
            if (uniqueStudentSet.size() > 0) {
                try {
                    ArrayList<String> responseDataUpdate = uploadStudentListToDB(uniqueStudentSet);
                    if (responseDataUpdate.get(0).equals("200")) {
                        LOGGER.info("[General information]: Added " + uniqueStudentSet.size() + " students to DB.");
                        savePlainStudentDataToFile(uniqueStudentSet);
                        EmailHandler.sendSecretKeyEmails(uniqueStudentSet);
                        EmailHandler.sendBackupEmail("data/json/student_data_backup.json");
                        addWarning("Alumnos añadidos a la base de datos.", 100, true);
                        controllerLogic();

                        asyncLoadStudentDataFromDB();
                    }
                } catch (IOException | NullPointerException e) {
                    System.err.println("[Error while uploading data to the DB]: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
                    LOGGER.severe("[Error while uploading data to the DB]: " + e.getMessage());
                    addWarning("Error subiendo datos.", 200, false);
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void savePlainStudentDataToFile(Set<Student> students) {
        try {
            JSONObject studentBackup = UtilParser.loadJSONObject("data/files/student_data_backup.json");
            for (Student student : students) {
                studentBackup.setString(student.getID(), student.toString());
            }
            parent.saveJSONObject(studentBackup, "data/files/student_data_backup.json");
        } catch (NullPointerException e) {
            LOGGER.severe("[Error while saving plain student data]: " + e.getMessage());
        }
    }

    private ArrayList<String> uploadStudentListToDB(Set<Student> uniqueStudentSet) throws IOException {
        Map<String, JSONObject> urlContentsMap = getHashKeyEmailMap(uniqueStudentSet);
        String combined = UtilParser.generateMultiPathJSONString(urlContentsMap);
        String baseURL = DatabaseHandler.getDatabaseDirectoryURL(model.getDBReference(), "");
        return dbHandler.updateData(baseURL, combined);
    }

    @NotNull
    private Map<String, JSONObject> getHashKeyEmailMap(Set<Student> uniqueStudentSet) {
        JSONObject hashKeyList = UtilParser.getStudentAttributeJSONObject(uniqueStudentSet, "hashKey");
        JSONObject emailList = UtilParser.getStudentAttributeJSONObject(uniqueStudentSet, "email");
        Map<String, JSONObject> urlContentsMap = new HashMap<>();
        urlContentsMap.put("Ids", hashKeyList);
        urlContentsMap.put("Emails", emailList);
        return urlContentsMap;
    }

    private void asyncRemoveStudentsFromDBButtonHook() {
        Runnable runnable = () -> {

            Set<Student> coincidentStudentSet = UtilParser.getIntersectionOfStudentSets(model.getTemporaryStudents(), model.getDBStudents());
            try {
                if (coincidentStudentSet.size() > 0) {
                    Map<String, JSONObject> urlContentsMap = getIDsEmailsUsersMap(coincidentStudentSet);
                    String combined = UtilParser.generateMultiPathJSONString(urlContentsMap);

                    String baseURL = DatabaseHandler.getDatabaseDirectoryURL(model.getDBReference(), "");
                    ArrayList<String> responseDataDelete = dbHandler.updateData(baseURL, combined);
                    if (responseDataDelete.get(0).equals("200")) {
                        LOGGER.info("[General information]: Removed " + coincidentStudentSet.size() + " students from DB.");
                        removePlainStudentDataFromFile(coincidentStudentSet);
                        model.getTemporaryStudents().clear();
                        addWarning("Borrados alumnos de la base de datos.", 250, true);
                        controllerLogic();

                        asyncLoadStudentDataFromDB();
                    }
                }

            } catch (IOException | NullPointerException e) {
                System.err.println("[Error while deleting data from the DB]: " + e.getMessage());
                LOGGER.severe("[Error while deleting data from the DB]: " + e.getMessage());
                addWarning("Error eliminando alumnos.", 200, false);
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void removePlainStudentDataFromFile(Set<Student> students) {
        try {
            JSONObject studentBackup = UtilParser.loadJSONObject("data/files/student_data_backup.json");
            for (Student student : students) {
                if (studentBackup.keys().contains(student.getID())) {
                    studentBackup.remove(student.getID());
                }
            }
            parent.saveJSONObject(studentBackup, "data/files/student_data_backup.json");
        } catch (NullPointerException e) {
            LOGGER.severe("[Error while saving plain student data]: " + e.getMessage());
        }
    }

    @NotNull
    private Map<String, JSONObject> getIDsEmailsUsersMap(Set<Student> coincidentStudentSet) {
        JSONObject nullList = UtilParser.getStudentAttributeJSONObject(coincidentStudentSet, null);
        Map<String, JSONObject> urlContentsMap = new HashMap<>();
        urlContentsMap.put("Ids", nullList);
        urlContentsMap.put("Emails", nullList);
        urlContentsMap.put("Users", nullList);
        return urlContentsMap;
    }

    private void asyncEmptyDBButtonHook() {
        Runnable runnable = () -> {
            try {
                String baseURL = DatabaseHandler.getDatabaseDirectoryURL(model.getDBReference(), "");
                ArrayList<String> response = dbHandler.emptyDB(baseURL);
                if (response.get(0).equals("200")) {
                    LOGGER.info("[General information]: Emptied data from DB.");
                    removePlainStudentDataFromFile(model.getDBStudents());
                    model.getDBStudents().clear();
                    addWarning("Base de datos vaciada.", 250, true);
                    controllerLogic();
                }
            } catch (IOException e) {
                System.err.println("[Error deleting data from DB]: " + e.getMessage());
                LOGGER.severe("[Error deleting data from DB]: " + e.getMessage());
                addWarning("Error vaciando la base de datos.", 200, false);
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void selectInputFile() {
        closedContextMenu = false;

        parent.selectInput("Seleccione el archivo de texto:", "selectInputFile");
        while (!closedContextMenu) {
            Thread.onSpinWait(); //We need to wait for the input file context menu to be closed before resuming execution
        }

        if (model.getInputFile().exists()) {
            try {
                Table studentIDTable = parent.loadTable(model.getInputFile().getAbsolutePath(), "header");
                Set<Student> studentList = generateStudentListFromTable(studentIDTable);

                model.addTemporaryStudentList(studentList);
            } catch (Exception e) {
                System.err.println("[Error loading the student list from CSV file]: " + e.getMessage());
                LOGGER.warning("[Error loading the student list from CSV file]: " + e.getMessage());
            }

        }
    }

    private Set<Student> generateStudentListFromTable(Table studentIDTable) {
        Set<Student> students = new HashSet<>();
        for (TableRow row : studentIDTable.rows()) {
            String id = row.getString(0);
            String email = row.getString("email");
            if (EmailHandler.isValidEmailAddress(email)) {
                try {
                    students.add(new Student(id, email));
                } catch (NullPointerException e) {
                    System.err.println("[Error while trying to insert new Student into temporary list]: " + e.getMessage());
                    LOGGER.severe("[Error while trying to insert new Student into temporary list]: " + e.getMessage());
                }
            }
        }
        return students;
    }

    private TextField getFocusedTextField() {
        TextField idTF = (TextField) view.getUIElement("idTF");
        TextField emailTF = (TextField) view.getUIElement("emailTF");
        if (idTF.isFocused()) {
            return idTF;
        } else {
            return emailTF;
        }
    }

    @Override
    public void onContextMenuClosed(File file) {
        model.setInputFile(file);
        controllerLogic();
    }
}
