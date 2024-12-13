package fun;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class AppDateFormatter {

    private final DateTimeFormatter formatter;

    private AppDateFormatter(String pattern) {
        formatter = DateTimeFormatter.ofPattern(pattern).withLocale(Locale.US);
    }

    public static AppDateFormatter mmddyy() {
        return new AppDateFormatter("mm-dd-yy");
    }

    public String format(Date date) {
        return formatter.format(date.toInstant());
    }
}
