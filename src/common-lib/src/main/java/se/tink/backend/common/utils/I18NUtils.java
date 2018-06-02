package se.tink.backend.common.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import se.tink.backend.core.Currency;
import se.tink.backend.core.Market;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;

public class I18NUtils {
    public class CurrencyFormat {
        // Value dependent format (exact if very small, rounded otherwise).
        public static final int DYNAMIC = 0x10;
        public static final int EXACT = 0x4;
        public static final int NONE = 0x0;
        public static final int ROUND = 0x8;
        public static final int SHORT = 0x2;
        public static final int VERY_SHORT = 0x16;

        // Include currency symbol.
        public static final int SYMBOL = 0x1;
    }

    // When using dynamic currency format:
    // if amount >= DYNAMIC_ROUNDING_THRESHOLD then rounded format is applied
    // else exact format is applied.
    private static final double DYNAMIC_ROUNDING_THRESHOLD = 10;

    private static final LoadingCache<Locale, DecimalFormat> FORMATTERS_EXACT_BY_LOCALE = CacheBuilder.newBuilder()
            .build(
                    new CacheLoader<Locale, DecimalFormat>() {
                        public DecimalFormat load(Locale locale) {
                            DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(locale);
                            formatter.applyPattern("###,##0.00");
                            return formatter;
                        }
                    });

    private static final LoadingCache<Locale, DecimalFormat> FORMATTERS_ROUND_BY_LOCALE = CacheBuilder.newBuilder()
            .build(
                    new CacheLoader<Locale, DecimalFormat>() {
                        public DecimalFormat load(Locale key) {
                            DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(key);
                            formatter.applyPattern("###,###");
                            formatter.setRoundingMode(RoundingMode.HALF_EVEN);
                            return formatter;
                        }
                    });

    private static final char NON_BREAKING_WHITESPACE = (char) 160;
    private static final ImmutableList<StringDoublePair> UNITS = ImmutableList.of(new StringDoublePair("k", 1000),
            new StringDoublePair("M", 1000000), new StringDoublePair("G", 1000000000));
    public static final String DEFAULT_LOCALE = "sv_SE";

    public static String formatAmountExact(double amount, Locale locale) {
        try {
            return FORMATTERS_EXACT_BY_LOCALE.get(locale).format(amount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatAmountRound(double amount, Locale locale) {
        try {
            return FORMATTERS_ROUND_BY_LOCALE.get(locale).format(amount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatCurrencyRound(double amount, Currency currency, Locale locale) {
        return formatCurrency(amount, currency, locale, CurrencyFormat.ROUND | CurrencyFormat.SYMBOL);
    }

    public static String formatCurrency(double amount, Currency currency, Locale locale) {
        return formatCurrency(amount, currency, locale, CurrencyFormat.DYNAMIC | CurrencyFormat.SYMBOL);
    }

    public static String formatCurrency(double amount, Currency currency, Locale locale, int currencyFormat) {
        double absValue = Math.abs(amount);

        String formatted = null;

        if ((currencyFormat & CurrencyFormat.ROUND) == CurrencyFormat.ROUND) {
            formatted = formatAmountRound(absValue, locale);
        } else if ((currencyFormat & CurrencyFormat.EXACT) == CurrencyFormat.EXACT) {
            formatted = formatAmountExact(absValue, locale);
        } else if ((currencyFormat & CurrencyFormat.SHORT) == CurrencyFormat.SHORT) {
            formatted = formatShort(absValue, locale);
        } else { // CurrencyFormat.DYNAMIC
            if (absValue < DYNAMIC_ROUNDING_THRESHOLD && absValue > 0) {
                formatted = formatAmountExact(absValue, locale);
            } else {
                formatted = formatAmountRound(absValue, locale);
            }
        }

        if ((currencyFormat & CurrencyFormat.SYMBOL) == CurrencyFormat.SYMBOL) {
            if (currency.isPrefixed()) {
                formatted = currency.getSymbol() + formatted;
            } else {
                formatted = formatted + NON_BREAKING_WHITESPACE + currency.getSymbol();
            }
        }

        if (amount < 0) {
            formatted = "-" + formatted;
        }

        return formatted;
    }

    public static String formatShort(double value, Locale locale) {
        StringDoublePair unit = null;

        for (StringDoublePair unitCandidate : UNITS) {
            if (value >= unitCandidate.getValue()) {
                unit = unitCandidate;
            } else {
                break;
            }
        }

        if (unit == null) {
            return String.format(locale, "%d", (int) value);
        }
        
        double valueInUnit = value / unit.getValue();
        
        // Check if value in unit is an integer.
        if (valueInUnit % 1 == 0) {
            // No decimals
            return String.format(locale, "%d%s", (int) valueInUnit, unit.getKey());
        } else {
            // One decimal
            return String.format(locale, "%.1f%s", Math.round(valueInUnit * 10) / 10d, unit.getKey());
        }
    }

    public static String numeral(Catalog catalog, int i) {
        switch (i) {
        case 0:
            return catalog.getString("zero");
        case 1:
            return catalog.getString("one");
        case 2:
            return catalog.getString("two");
        case 3:
            return catalog.getString("three");
        case 4:
            return catalog.getString("four");
        case 5:
            return catalog.getString("five");
        case 6:
            return catalog.getString("six");
        case 7:
            return catalog.getString("seven");
        case 8:
            return catalog.getString("eight");
        case 9:
            return catalog.getString("nine");
        default:
            return Integer.toString(i);
        }
    }

    public static Locale getLocale(String locale) {
        return new Locale(locale.substring(0, 2), locale.substring(3, 5));
    }

    public static String getMonthShortName(Catalog catalog, Locale locale, String dateAsString) {
        return DateUtils.convertDate(dateAsString).toString(catalog.getString("MMM"), locale);
    }

    public static String getWeekdayShortName(Catalog catalog, Locale locale, int dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        String dayOfWeekName = new SimpleDateFormat("EEEE", locale).format(calendar.getTime());
        if (dayOfWeekName.length() > 3) {
            dayOfWeekName = dayOfWeekName.substring(0, 3);
        }
        return dayOfWeekName;
    }

    public static String formatShortDate(Catalog catalog, Locale locale, DateTime date) {
        return StringUtils.formatHuman(getShortMonthDayFormat(catalog, locale, date.toDate()));
    }

    public static String formatShortDate(Catalog catalog, Locale locale, Date date) {
        return formatShortDate(catalog, locale, new DateTime(date));
    }

    public static String formatVeryShortDate(Catalog catalog, Locale locale, DateTime date) {
        // Note, please note that this can be changed in OneSky
        return StringUtils.formatHuman(date.toString(catalog.getString("M/d"), locale));
    }

    public static String formatVeryShortDate(Catalog catalog, Locale locale, Date date) {
        return formatVeryShortDate(catalog, locale, new DateTime(date));
    }

    public static String humanDateFormat(Catalog catalog, Locale locale, Date date) {

        int dateDiff = DateUtils.daysBetween(new Date(), date);

        // Today
        if (dateDiff == 0) {
            return catalog.getString("today");
        }

        if (dateDiff == -1) {
            return catalog.getString("yesterday");
        }

        if (dateDiff == 1) {
            return catalog.getString("tomorrow");
        }

        // Weekday format
        if (Math.abs(dateDiff) < 7) {

            String message;

            if (dateDiff < 0) {
                message = catalog.getString("last {0}");
            } else {
                message = catalog.getString("on {0}");
            }

            return Catalog.format(message, new ThreadSafeDateFormat("EEEE", locale).format(date));
        }

        // Same year
        if (DateTime.now().year().equals(new DateTime(date).year())) {
            return getShortMonthDayFormat(catalog, locale, date);
        }

        // Default with year
        return new ThreadSafeDateFormat(catalog.getString("MMM d YYYY"), locale).format(date);
    }
    
    public static String humanDateShortFormat(Catalog catalog, Locale locale, Date date) {

        int dateDiff = DateUtils.daysBetween(new Date(), date);

        if (dateDiff == 0) {
            // Today

            return catalog.getString("today");
        } else if (dateDiff < 7 && dateDiff > -7) {
            // Within a week

        	String dateString = new ThreadSafeDateFormat("EEEE", locale).format(date);
            if (dateString.length() > 3) {
                dateString = dateString.substring(0, 3);
            }
            return dateString;
        }

        return getShortMonthDayFormat(catalog, locale, date);
    }

    public static DayOfWeek getDayOfWeek(Locale locale, String shortName) throws ParseException {
        SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE", locale);
        return DateUtils.getDayOfWeek(weekdayFormat.parse(shortName));
    }

    public static String getPluralizedWeekdayName(Catalog catalog, DayOfWeek dayOfWeek) {
        switch(dayOfWeek) {
        case MONDAY:
            return catalog.getString("Mondays");
        case TUESDAY:
            return catalog.getString("Tuesdays");
        case WEDNESDAY:
            return catalog.getString("Wednesdays");
        case THURSDAY:
            return catalog.getString("Thursdays");
        case FRIDAY:
            return catalog.getString("Fridays");
        case SATURDAY:
            return catalog.getString("Saturdays");
        case SUNDAY:
            return catalog.getString("Sundays");
        default:
            return null;
        }
    }

    private static String getShortMonthDayFormat(Catalog catalog, Locale locale, Date date) {
        // Note, please not that this can be changed in onesky
        return new ThreadSafeDateFormat(catalog.getString("MMM d"), locale).format(date);
    }

    /**
     * Join a list in human format (i.e. "X, Y and Z")
     * 
     * @param parts
     * @param catalog
     * @return
     */
    public static String joinHuman(Catalog catalog, List<String> parts)
    {
        String result = null;

        if (parts.size() > 1)
        {
            result = Joiner.on(", ").join(parts.subList(0, parts.size() - 1));
            result += " " + catalog.getString("and") + " " + parts.get(parts.size() - 1);
        }
        else
        {
            result = parts.get(0);
        }

        return result;
    }

    public static DateTime getNow(Calendar calendar)
    {
        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(calendar.getTimeZone());
        return DateTime.now(dateTimeZone);
    }

    public static boolean isToday(DateTime date, Calendar calendar) {
        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(calendar.getTimeZone());
        DateTime now = DateTime.now(dateTimeZone);

        return isSameDay(now, date);
    }

    public static boolean isSameDay(DateTime date1, DateTime date2)
    {
        return (date1.getYear() == date2.getYear() && date1.getDayOfYear() == date2.getDayOfYear());
    }
    
    public static boolean isSameDay(Date date1, Date date2)
    {
        return isSameDay(new DateTime(date1), new DateTime(date2));
    }
    
    // This should probably be a property directly on the `Market` object, or a configurable property in `UserProfile`.
    public static int getFirstDayOfWeek(Market market) {
        if (Objects.equal(market.getCodeAsString(), "US")) {
            return Calendar.SUNDAY;
        } else {
            return Calendar.MONDAY;
        }
    }
}
