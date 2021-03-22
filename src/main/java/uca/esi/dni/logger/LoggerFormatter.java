package uca.esi.dni.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggerFormatter extends Formatter {

    public String format(LogRecord rec) {
        StringBuilder buf = new StringBuilder(1000);
        buf.append("[");
        buf.append(calcDate(rec.getMillis()));
        buf.append("]::[");
        buf.append(rec.getLevel());
        buf.append("]\t (");
        buf.append(rec.getSourceClassName());
        buf.append("): ");
        buf.append(rec.getMessage());
        buf.append("\n");

        return buf.toString();
    }


    private String calcDate(long millis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEEEEEE\t, dd/MM/yyyy kk:mm");
        Date resultDate = new Date(millis);
        return dateFormat.format(resultDate);
    }
}
