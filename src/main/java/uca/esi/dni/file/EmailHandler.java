package uca.esi.dni.file;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import processing.data.JSONObject;
import uca.esi.dni.main.DniParser;
import uca.esi.dni.types.Student;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class EmailHandler {
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static String sender;
    private static String username;
    private static String password;
    private static String backupEmail;
    private static final String HOST = "smtp.gmail.com";

    private static final String GENERAL_HEADER_TEXT = "Clave segura para aplicación de Manual de Laboratorio Escuela Superior de Ingeniería";
    private static final String GENERAL_CONTENT_TEXT = "<h1>Clave de acceso al manual de laboratorio de la Escuela Superior de Ingeniería</h1>" +
            "<p>Has recibido este correo porque se te ha incluido en la lista de alumnos con acceso a la aplicación " +
            "Android de manual de laboratorio de Resistencia de Materiales de la Escuela Superior de Ingeniería.</p>" +
            "<p><b>Tu clave de acceso es: %s .</b></p>" +
            "<p>Esta clave, junto con tu identificador único de la UCA (%s), son necesarios para acceder a la aplicación.</p>" +
            "<p>Este mensaje ha sido generado de forma automática. Si has recibido este mensaje por error, puedes ignorarlo. Si tu identificador único de la UCA " +
            "no coincide con el mostrado en el mensaje, envía un correo electrónico a la dirección: %s .</p>";

    private static final String BACKUP_HEADER_TEXT = "Archivo de copia de seguridad de datos de alumnos [%s]";
    private static final String BACKUP_CONTENT_TEXT = "<p>Se adjunta la lista de contraseñas en texto plano de los alumnos añadidos a la base de datos.</p>";

    private EmailHandler() {
    }

    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    public static void sendSecretKeyEmails(Set<Student> students) {
        Map<String, String> emailContentMap = new HashMap<>();
        for (Student student : students) {
            String formattedMessage = String.format(GENERAL_CONTENT_TEXT, student.getKey(), student.getId(), sender);
            emailContentMap.put(student.getEmail(), formattedMessage);
        }
        sendEmailCollection(emailContentMap);
        String toLog = "[Mensaje/s enviado/s correctamente]: Numero de recipientes:" + students.size();
        LOGGER.info(toLog);
    }

    public static void sendBackupEmail(String filepath) {
        try {
            sendBackupEmail(new File(filepath));
        } catch (NullPointerException e) {
            LOGGER.severe("[Error while sending student data backup email]: " + e.getMessage());
        }
    }

    public static void sendBackupEmail(File attachment) {
        Session session = getSessionObject();
        String header = String.format(BACKUP_HEADER_TEXT, new Date().toString());
        try {
            sendHTMLEmail(backupEmail, header, BACKUP_CONTENT_TEXT, attachment, session);
        } catch (MessagingException e) {
            LOGGER.severe("[Error sending email]: " + e.getMessage());
        }
        LOGGER.info("[Mensaje enviado correctamente]: Copia de seguridad de alumnos introducidos.");
    }

    private static void sendEmailCollection(Map<String, String> emailContentMap) {
        Session session = getSessionObject();
        for (Map.Entry<String, String> entry : emailContentMap.entrySet()) {
            try {
                sendHTMLEmail(entry.getKey(), EmailHandler.GENERAL_HEADER_TEXT, entry.getValue(), session);
            } catch (MessagingException e) {
                LOGGER.severe("[Error sending email]: " + e.getMessage());
            }
        }
    }

    private static Session getSessionObject() {
        Properties props = generatePropertiesObject();
        //create the Session object
        return Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
    }

    private static Properties generatePropertiesObject() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", "587");
        return props;
    }

    private static void sendHTMLEmail(String dest, String header, String contentText, Session session) throws MessagingException {
        sendHTMLEmail(dest, header, contentText, null, session);
    }

    private static void sendHTMLEmail(String dest, String header, String contentText, File attachment, Session session) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(dest));
        message.setSubject(header);
        setMultiPartContent(message, contentText, attachment);
        Transport.send(message);
    }

    private static void setMultiPartContent(Message message, String textContent, File attachment) throws MessagingException {
        Multipart multipart = new MimeMultipart();
        BodyPart messageTextBodyPart = new MimeBodyPart();
        messageTextBodyPart.setContent(textContent, "text/html; charset=UTF-8");
        multipart.addBodyPart(messageTextBodyPart);
        if (attachment != null && attachment.exists()) {
            BodyPart attachmentBodyPart = getAttachmentBodyPart(attachment);
            multipart.addBodyPart(attachmentBodyPart);
        }
        message.setContent(multipart);
    }

    public static BodyPart getAttachmentBodyPart(File filename) throws MessagingException {
        BodyPart messageBodyPart = new MimeBodyPart();

        // specify your file
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename.getName());

        return messageBodyPart;
    }

    public static void loadSettings() {
        JSONObject settingsObject = Util.loadJSONObject(DniParser.SETTINGS_FILEPATH);
        sender = settingsObject.getString("senderEmail");
        username = settingsObject.getString("senderUsername");
        password = settingsObject.getString("senderPassword");
        backupEmail = settingsObject.getString("backupEmail");
    }
}
