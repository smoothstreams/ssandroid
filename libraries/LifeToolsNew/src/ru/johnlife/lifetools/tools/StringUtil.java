package ru.johnlife.lifetools.tools;

import java.util.List;

/**
 * Created by yanyu on 5/21/2016.
 */
public class StringUtil {
    public interface Transformer<T> {
        String transform(T object);
    }

    public static <T> String implode(List<T> what, String delimeter, Transformer<T> transformer) {
        if (what == null) return "";
        String[] array = new String[what.size()];
        int i = 0;
        for (T object : what) {
            array[i++] = transformer.transform(object);
        }
        return implode(array, delimeter);
    }

    public static String implode(List<String> what, String delimeter) {
        if (what == null) return "";
        return implode(what.toArray(new String[]{}), delimeter);
    }

    public static String implode(String[] what, String delimeter) {
        if (what == null) return "";
        StringBuilder b = new StringBuilder();
        for (String s : what) {
            b.append(s).append(delimeter);
        }
        b.delete(b.length()-delimeter.length(),b.length());
        return b.toString();
    }
}
