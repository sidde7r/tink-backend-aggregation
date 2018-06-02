package se.tink.backend.export.helper;

import java.util.Date;
import se.tink.libraries.date.DateUtils;

/**
 * Defines the default return value in case of null
 */
public interface DefaultSetter {
    default String notNull(String string){
        if (string == null) {
            string = "";
        }
        return string;
    }

    default String notNull(Date date){
        if (date != null) {
            return notNull(DateUtils.toISO8601Format(date));
        }
        return "";
    }

    default String notNull(Integer val) {
        if (val == null) {
            return "";
        }
        return Integer.toString(val);
    }

    default String notNull(Double val) {
        if (val == null) {
            return "";
        }
        return Double.toString(val);
    }

    default String notNull(Boolean bool) {
        if (bool == null) {
            return "";
        }
        return Boolean.toString(bool);
    }

    default <T> String notNull(T t){
        if (t == null){
            return "";
        }

        if (t instanceof Enum<?>) {
            return ((Enum) t).name();
        }

        return toString();
    }

}

