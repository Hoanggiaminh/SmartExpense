package com.example.smartexpense.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    private static final Locale VIETNAMESE_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(VIETNAMESE_LOCALE);

    /**
     * Định dạng số tiền theo định dạng VND
     * @param amount số tiền cần định dạng
     * @return chuỗi đã được định dạng theo VND
     */
    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Định dạng số tiền theo định dạng VND
     * @param amount số tiền cần định dạng (long)
     * @return chuỗi đã được định dạng theo VND
     */
    public static String formatCurrency(long amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Định dạng số tiền theo định dạng VND
     * @param amount số tiền cần định dạng (int)
     * @return chuỗi đã được định dạng theo VND
     */
    public static String formatCurrency(int amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Định dạng số tiền mà không có ký hiệu tiền tệ, chỉ có dấu phẩy phân cách
     * @param amount số tiền cần định dạng
     * @return chuỗi số đã được định dạng với dấu phẩy
     */
    public static String formatNumber(double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(VIETNAMESE_LOCALE);
        return numberFormat.format(amount);
    }

    /**
     * Định dạng số tiền mà không có ký hiệu tiền tệ, chỉ có dấu phẩy phân cách
     * @param amount số tiền cần định dạng (long)
     * @return chuỗi số đã được định dạng với dấu phẩy
     */
    public static String formatNumber(long amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(VIETNAMESE_LOCALE);
        return numberFormat.format(amount);
    }
}
