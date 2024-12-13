package fun;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class AppNumberFormatter {

    // ThreadLocal to maintain a separate NumberFormat instance per thread
    private final ThreadLocal<NumberFormat> formatThreadLocal = ThreadLocal.withInitial(() -> null);

    // Private constructor to enforce the use of factory methods
    private AppNumberFormatter(NumberFormat format) {
        formatThreadLocal.set(format);
    }

    // Factory method for creating an instance with specific decimal places
    public static AppNumberFormatter withDecimals(int decimals) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(decimals);
        format.setMinimumFractionDigits(decimals);
        return new AppNumberFormatter(format);
    }

    // Factory method for percent instance
    public static AppNumberFormatter percent(int decimals) {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(decimals);
        format.setMinimumFractionDigits(decimals);
        ((DecimalFormat)format).setNegativePrefix("$(");
        ((DecimalFormat)format).setNegativeSuffix(")");
        return new AppNumberFormatter(format);
    }

    // Format method using the NumberFormat instance from ThreadLocal
    public String format(Number num) {
        return formatThreadLocal.get().format(num);
    }
}
