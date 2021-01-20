package se.tink.libraries.date;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Immutable thread safe date format.
 *
 * <p>Uses the Memento design pattern to easily clone preexisting static thread safe date
 * formatters.
 */
public class ThreadSafeDateFormat {

    private static final CharMatcher TRIMMER = CharMatcher.whitespace();

    public static final ThreadSafeDateFormat FORMATTER_YEARLY = new ThreadSafeDateFormat("yyyy");
    public static final ThreadSafeDateFormat FORMATTER_WEEK_YEARLY =
            new ThreadSafeDateFormat("xxxx");
    public static final ThreadSafeDateFormat FORMATTER_MONTHLY =
            new ThreadSafeDateFormat("yyyy-MM");
    public static final ThreadSafeDateFormat FORMATTER_INTEGER_DATE =
            new ThreadSafeDateFormat("yyyyMMdd");
    public static final ThreadSafeDateFormat FORMATTER_INTEGER_DATE_COMPACT =
            new ThreadSafeDateFormat("yyMMdd");
    public static final ThreadSafeDateFormat FORMATTER_MONTHLY_ONLY =
            new ThreadSafeDateFormat("MM");
    public static final ThreadSafeDateFormat FORMATTER_MONTH_NAME =
            new ThreadSafeDateFormat("MMMMM");
    public static final ThreadSafeDateFormat FORMATTER_WEEKLY =
            new ThreadSafeDateFormat("xxxx:ww"); // weekyear
    public static final ThreadSafeDateFormat FORMATTER_DAILY =
            new ThreadSafeDateFormat("yyyy-MM-dd");
    public static final ThreadSafeDateFormat FORMATTER_DAILY_DEFAULT_TIMEZONE =
            FORMATTER_DAILY.builder.setTimezone(TimeZone.getDefault()).build();
    public static final ThreadSafeDateFormat FORMATTER_DD_MM_YYYY =
            new ThreadSafeDateFormat("dd/MM/yyyy");
    public static final ThreadSafeDateFormat FORMATTER_DOTTED_DAILY =
            new ThreadSafeDateFormat("dd.MM.yyyy");
    public static final ThreadSafeDateFormat FORMATTER_DAILY_COMPACT =
            new ThreadSafeDateFormat("yy-MM-dd");
    public static final ThreadSafeDateFormat FORMATTER_DAILY_PRETTY =
            new ThreadSafeDateFormat("MMM d");
    public static final ThreadSafeDateFormat FORMATTER_MINUTES =
            new ThreadSafeDateFormat("yyyy-MM-dd HH:mm");
    public static final ThreadSafeDateFormat FORMATTER_SECONDS =
            new ThreadSafeDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final ThreadSafeDateFormat FORMATTER_SECONDS_DASHES =
            new ThreadSafeDateFormat("yyyy-MM-dd--HH:mm:ss");
    public static final ThreadSafeDateFormat FORMATTER_SECONDS_T =
            new ThreadSafeDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final ThreadSafeDateFormat FORMATTER_SECONDS_WITH_TIMEZONE =
            new ThreadSafeDateFormat("yyyy-MM-dd HH:mm:ssZ");
    public static final ThreadSafeDateFormat FORMATTER_MINS_WITH_TIMEZONE =
            new ThreadSafeDateFormat("yyyy-MM-dd HH:mmZ");
    public static final ThreadSafeDateFormat FORMATTER_MILLISECONDS_WITH_TIMEZONE =
            new ThreadSafeDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final ThreadSafeDateFormat FORMATTER_MILLISECONDS_WITHOUT_TIMEZONE =
            new ThreadSafeDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final ThreadSafeDateFormat FORMATTER_TIME_MILLIS =
            new ThreadSafeDateFormat("HH:mm:ss,SSS");
    public static final ThreadSafeDateFormat FORMATTER_FILENAME_SAFE =
            new ThreadSafeDateFormat("yyyy-MM-dd--HH:mm:ss.SSS");
    public static final ThreadSafeDateFormat FORMATTER_LOGGING =
            new ThreadSafeDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    /**
     * A builder used to construct {@link ThreadSafeDateFormat}s. Mostly used to create variations
     * of a preexisting {@link ThreadSafeDateFormat} by calling {@link
     * ThreadSafeDateFormat#toBuilder()}.
     */
    public static class ThreadSafeDateFormatBuilder implements Cloneable {

        private String pattern;
        private Locale locale;
        private TimeZone timezone;

        public ThreadSafeDateFormatBuilder(String pattern, Locale locale, TimeZone timezone) {
            setPattern(pattern);
            setLocale(locale);
            setTimezone(timezone);
        }

        private ThreadSafeDateFormatBuilder setTimezone(TimeZone timezone) {
            this.timezone = Preconditions.checkNotNull(timezone);
            return this;
        }

        public String getPattern() {
            return pattern;
        }

        public ThreadSafeDateFormatBuilder setPattern(String pattern) {
            this.pattern = Preconditions.checkNotNull(pattern);
            return this;
        }

        public Locale getLocale() {
            return locale;
        }

        public ThreadSafeDateFormatBuilder setLocale(Locale locale) {
            this.locale = Preconditions.checkNotNull(locale);
            return this;
        }

        @Override
        public ThreadSafeDateFormatBuilder clone() {
            try {
                return (ThreadSafeDateFormatBuilder) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Could not clone. Unexpected.", e);
            }
        }

        public ThreadSafeDateFormat build() {
            // Cloning this to make things immutable.
            return new ThreadSafeDateFormat(this.clone());
        }

        public TimeZone getTimezone() {
            return timezone;
        }
    }

    private DateTimeFormatter dateFormat;

    private ThreadSafeDateFormatBuilder builder;

    public ThreadSafeDateFormat(String pattern) {
        this(
                new ThreadSafeDateFormatBuilder(
                        pattern,
                        new Locale(
                                CountryDateHelper.LANGUAGE_CODE_SWEDISH,
                                CountryDateHelper.COUNTRY_CODE_SWEDEN),
                        TimeZone.getTimeZone(CountryDateHelper.TIMEZONE_CODE_CET)));
    }

    public ThreadSafeDateFormat(String pattern, Locale locale) {
        this(
                new ThreadSafeDateFormatBuilder(
                        pattern,
                        locale,
                        TimeZone.getTimeZone(CountryDateHelper.TIMEZONE_CODE_CET)));
    }

    public ThreadSafeDateFormat(String pattern, Locale locale, TimeZone timezone) {
        this(new ThreadSafeDateFormatBuilder(pattern, locale, timezone));
    }

    private ThreadSafeDateFormat(ThreadSafeDateFormatBuilder builder) {
        this.builder = builder;
        this.dateFormat =
                DateTimeFormat.forPattern(builder.getPattern())
                        .withZone(DateTimeZone.forTimeZone(builder.getTimezone()))
                        .withLocale(builder.getLocale());
    }

    public String format(Date date) {
        return dateFormat.print(date.getTime());
    }

    public String format(ReadablePartial date) {
        return dateFormat.print(date);
    }

    public String format(Instant instant) {
        return dateFormat.print(Date.from(instant).getTime());
    }

    public Date parse(String string) throws ParseException {
        try {
            return dateFormat.parseDateTime(TRIMMER.trimFrom(string)).toDate();
        } catch (Exception e) {
            throw new ParseException(
                    "could not parse date: "
                            + string
                            + " (with pattern: "
                            + builder.getPattern()
                            + ")",
                    0);
        }
    }

    public boolean fitsFormat(String period) {
        try {
            dateFormat.parseDateTime(period);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ThreadSafeDateFormatBuilder toBuilder() {
        // Cloning since we don't allow internal builder to be mutable.
        return builder.clone();
    }
}
