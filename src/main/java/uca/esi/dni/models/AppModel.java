package uca.esi.dni.models;

import uca.esi.dni.data.Student;
import uca.esi.dni.ui.Warning;

import java.io.File;
import java.util.ArrayList;

public class AppModel extends Model {
    private final ArrayList<Student> DBStudents = new ArrayList<>();
    private final ArrayList<Student> modifiedStudents = new ArrayList<>();
    private File inputFile;
    private File outputFolder;
    private String DBReference;
    private final ArrayList<Warning> warnings = new ArrayList<>();

    public AppModel() {
        //testing item list display
        for (int i = 0; i < 13; i++) {
            DBStudents.add(new Student(String.valueOf(i * 100), "test@", i));
        }
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public String getDBReference() {
        return DBReference;
    }

    public void setDBReference(String DBReference) {
        this.DBReference = DBReference;
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public ArrayList<Student> getDBStudents() {
        return DBStudents;
    }

    public void addDBStudent(Student s) {
        DBStudents.add(s);
    }

    public void removeDBStudent(Student s) {
        DBStudents.remove(s);
    }

    public Student getDBStudent(int index) {
        return DBStudents.get(index);
    }

    public ArrayList<Student> getModifiedStudents() {
        return modifiedStudents;
    }

    public void addModifiedStudent(Student s) {
        modifiedStudents.add(s);
    }

    public void removeModifiedStudent(Student s) {
        modifiedStudents.remove(s);
    }

    public Student getModifiedStudent(int index) {
        return modifiedStudents.get(index);
    }

    public ArrayList<Warning> getWarnings() {
        return warnings;
    }

    public void addWarning(Warning w) {
        warnings.add(w);
    }

    public void removeWarning(Warning w) {
        warnings.remove(w);
    }

    public Warning getWarning(int index) {
        return warnings.get(index);
    }

}