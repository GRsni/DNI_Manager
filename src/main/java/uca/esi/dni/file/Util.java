package uca.esi.dni.file;

import com.google.common.hash.Hashing;
import org.jetbrains.annotations.NotNull;
import processing.core.PApplet;
import processing.data.JSONObject;
import processing.data.Table;
import processing.data.TableRow;
import uca.esi.dni.types.JSONParsingException;
import uca.esi.dni.types.Student;
import uca.esi.dni.types.Survey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static final Pattern idPattern = Pattern.compile("u[a-zA-Z0-9]{8}");
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private Util() {
    }

    public static String extractId(String line) {

        Matcher m = idPattern.matcher(line);
        String match = "";

        // if we find a match, get the group
        if (m.find()) {
            match = m.group(0);
        }
        return match;
    }

    public static boolean checkFileExtension(String file, String ext) {
        String fileExtension = PApplet.checkExtension(file);
        if (fileExtension == null) {
            return false;
        } else {
            return fileExtension.equals(ext);
        }
    }

    @NotNull
    public static String getSHA256HashedString(String plain) {
        return Hashing.sha256().hashString(plain, StandardCharsets.UTF_8).toString();
    }

    public static JSONObject loadJSONObject(String filepath) {
        if (filepath != null && checkFileExtension(filepath, "json")) {
            try {
                InputStream inputStream = Util.class.getClassLoader().getResourceAsStream(filepath);
                String fileContent = readFromInputStream(inputStream);
                return parseJSONObject(fileContent);
            } catch (IOException | NullPointerException e) {
                LOGGER.severe("[Error while reading JSON file]: " + e.getMessage());
                return new JSONObject();
            }
        }
        return new JSONObject();
    }

    public static JSONObject parseJSONObject(String text) {
        if (text != null && !text.isEmpty() && !text.equals("null")) {
            return JSONObject.parse(text);
        } else {
            return new JSONObject();
        }
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static Map<String, Map<String, Table>> createStudentsDataTables(JSONObject allStudentsList) throws NullPointerException {
        Map<String, Map<String, Table>> studentTableMap = new HashMap<>();
        List<String> ids = getJSONObjectKeys(allStudentsList);
        for (String id : ids) {
            Map<String, Table> tableMap = new HashMap<>();
            JSONObject studentData = allStudentsList.getJSONObject(id);

            JSONObject labs = studentData.getJSONObject("practicas");
            if (labs != null) {
                List<String> labTypes = getJSONObjectKeys(labs);
                for (String labType : labTypes) {
                    JSONObject labRun = labs.getJSONObject(labType);
                    if (labRun != null) {
                        Table dataTable = parseJSONDataIntoTable(labRun);
                        tableMap.put(labType, dataTable);
                    }
                }
                studentTableMap.put(id, tableMap);
            }
        }
        return studentTableMap;
    }

    private static List<String> getJSONObjectKeys(JSONObject object) throws NullPointerException {
        List<String> keys = new ArrayList<>();

        for (Object key : object.keys()) {
            keys.add((String) key);
        }
        return keys;
    }

    private static Table parseJSONDataIntoTable(JSONObject jsonObject) {
        Table table = new Table();
        List<String> entries = getJSONObjectKeys(jsonObject);
        List<String> entryKeys = getJSONObjectKeys(jsonObject.getJSONObject(entries.get(0)));

        for (String key : entryKeys) {
            table.addColumn(key);
        }

        for (String entry : entries) {
            JSONObject entryJSONObject = jsonObject.getJSONObject(entry);
            TableRow row = table.addRow();
            populateRowFromLabRunObject(entryJSONObject, row);
        }

        return table;
    }

    private static void populateRowFromLabRunObject(JSONObject jsonObject, TableRow row) {
        for (String k : getJSONObjectKeys(jsonObject)) {
            try {
                Object v = jsonObject.get(k);
                if (v instanceof Integer || v instanceof Long) {
                    long intToUse = ((Number) v).longValue();
                    row.setLong(k, intToUse);
                } else if (v instanceof Boolean) {
                    boolean boolToUse = (Boolean) v;
                    row.setString(k, Boolean.toString(boolToUse));
                } else if (v instanceof Float || v instanceof Double) {
                    double floatToUse = ((Number) v).doubleValue();
                    row.setDouble(k, floatToUse);
                } else if (JSONObject.NULL.equals(v)) {
                    row.setString(k, "null");
                } else {
                    String stringToUse = jsonObject.getString(k);
                    row.setString(k, stringToUse);
                }
            } catch (Exception e2) {
                LOGGER.warning("[Exception while reading JSONObject]: " + e2.getMessage());
            }
        }

    }

    public static String generateMultiPathJSONString(@NotNull Map<String, JSONObject> urlContentsMap) throws NullPointerException {
        JSONObject multipath = new JSONObject();
        for (Map.Entry<String, JSONObject> entry : urlContentsMap.entrySet()) {
            List<String> secondLevelKeys = getJSONObjectKeys(urlContentsMap.get(entry.getKey()));
            for (String key : secondLevelKeys) {
                multipath.put(entry.getKey() + "/" + key, getStringValueInJSONObject(entry.getValue(), key));
            }
        }
        return multipath.toString();
    }

    private static Object getStringValueInJSONObject(JSONObject jsonObject, String key) {
        if (jsonObject == null || jsonObject.size() == 0) {
            return null;
        } else if (jsonObject.isNull(key)) {
            return JSONObject.NULL;
        } else {
            return jsonObject.getString(key);
        }
    }

    public static JSONObject getStudentAttributeJSONObject(Set<Student> students, String attribute) {
        JSONObject jsonObject = new JSONObject();
        for (Student student : students) {
            if (attribute != null) {
                jsonObject.setString(student.getId(), student.getAttributeFromStudent(attribute));
            } else {
                jsonObject.put(student.getId(), JSONObject.NULL);
            }
        }
        return jsonObject;
    }

    public static Set<Student> generateStudentListFromJSONObject(JSONObject hashKeys, JSONObject emails) throws
            JSONParsingException {
        Set<Student> students = new HashSet<>();
        List<String> ids = getJSONObjectKeys(emails);
        for (String id : ids) {
            try {
                Student student = new Student(id, emails.getString(id), hashKeys.getString(id));
                students.add(student);
            } catch (RuntimeException e) {
                throw new JSONParsingException(e.getMessage());
            }
        }
        return students;
    }

    public static List<Survey> generateSurveyListFromJSONObject(JSONObject surveysJSONObject)
            throws JSONParsingException {
        List<Survey> surveyList = new ArrayList<>();
        List<String> keys = getJSONObjectKeys(surveysJSONObject);
        for (String surveyId : keys) {
            surveyList.add(new Survey(surveyId, surveysJSONObject.getJSONObject(surveyId)));
        }
        return surveyList;
    }

    public static Set<String> studentSetToStringSet(@NotNull Set<Student> students) throws NullPointerException {
        Set<String> stringSet = new HashSet<>();
        for (Student s : students) {
            stringSet.add(s.getId());
        }
        return stringSet;
    }

    public static Set<Student> getUniqueStudentSet(Set<Student> set1, Set<Student> set2) throws NullPointerException {
        if (set1 == null || set2 == null) {
            throw new NullPointerException("Student set cannot be null");
        } else {
            Set<Student> unique = new HashSet<>();
            for (Student s : set1) {
                boolean found = false;
                for (Student s2 : set2) {
                    if (s.getId().equals(s2.getId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    unique.add(s);
                }
            }
            return unique;
        }
    }

    public static Set<Student> getIntersectionOfStudentSets(Set<Student> set1, Set<Student> set2) throws NullPointerException {
        if (set1 == null || set2 == null) {
            throw new NullPointerException("Student set cannot be null");
        } else {
            Set<Student> coincident = new HashSet<>();
            for (Student s : set1) {
                for (Student s2 : set2) {
                    if (s.getId().equals(s2.getId())) {
                        coincident.add(s2);
                        break;
                    }
                }
            }
            return coincident;
        }
    }
}