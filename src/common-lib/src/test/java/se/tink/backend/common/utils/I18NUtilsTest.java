package se.tink.backend.common.utils;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.Locale;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.core.Currency;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class I18NUtilsTest {
    private static final Currency CURRENCY_USD = new Currency("USD", "$", true, 1);
    private static final Currency CURRENCY_EUR = new Currency("EUR", "€", true, 1);
    private static final Currency CURRENCY_SEK = new Currency("SEK", "kr", false, 10);
    private static final Locale LOCALE_SE = new Locale("sv", "SE");
    private static final Locale LOCALE_NL = new Locale("nl", "NL");
    private static final Locale LOCALE_US = new Locale("en", "US");

    @Test
    public void formatCurrency() {

        // SEK
        Assert.assertEquals("15 700 kr", I18NUtils.formatCurrency(15700, CURRENCY_SEK, LOCALE_SE));
        Assert.assertEquals("157 kr", I18NUtils.formatCurrency(156.60, CURRENCY_SEK, LOCALE_SE));
        Assert.assertEquals("-157 kr", I18NUtils.formatCurrency(-156.60, CURRENCY_SEK, LOCALE_SE));
        Assert.assertEquals("6,60 kr", I18NUtils.formatCurrency(6.60, CURRENCY_SEK, LOCALE_SE));
        Assert.assertEquals("156,60", I18NUtils.formatCurrency(156.601, CURRENCY_SEK, LOCALE_SE,
                I18NUtils.CurrencyFormat.EXACT));
        Assert.assertEquals("0,60 kr", I18NUtils.formatCurrency(0.60, CURRENCY_SEK, LOCALE_SE));

        // USD
        Assert.assertEquals("$15,700", I18NUtils.formatCurrency(15700, CURRENCY_USD, LOCALE_US));
        Assert.assertEquals("$157", I18NUtils.formatCurrency(156.60, CURRENCY_USD, LOCALE_US));
        Assert.assertEquals("-$157", I18NUtils.formatCurrency(-156.60, CURRENCY_USD, LOCALE_US));
        Assert.assertEquals("$6.60", I18NUtils.formatCurrency(6.60, CURRENCY_USD, LOCALE_US));
        Assert.assertEquals("$0.60", I18NUtils.formatCurrency(0.60, CURRENCY_USD, LOCALE_US));
        Assert.assertEquals("156.60", I18NUtils.formatCurrency(156.601, CURRENCY_USD, LOCALE_US,
                I18NUtils.CurrencyFormat.EXACT));

        // EUR
        Assert.assertEquals("€15.700", I18NUtils.formatCurrency(15700, CURRENCY_EUR, LOCALE_NL));
        Assert.assertEquals("€157", I18NUtils.formatCurrency(156.60, CURRENCY_EUR, LOCALE_NL));
        Assert.assertEquals("-€157", I18NUtils.formatCurrency(-156.60, CURRENCY_EUR, LOCALE_NL));
        Assert.assertEquals("€6,60", I18NUtils.formatCurrency(6.60, CURRENCY_EUR, LOCALE_NL));
        Assert.assertEquals("156,60", I18NUtils.formatCurrency(156.601, CURRENCY_EUR, LOCALE_NL,
                I18NUtils.CurrencyFormat.EXACT));
        Assert.assertEquals("€0,60", I18NUtils.formatCurrency(0.60, CURRENCY_EUR, LOCALE_NL));
        Assert.assertEquals("0,60",
                I18NUtils.formatCurrency(0.60, CURRENCY_EUR, LOCALE_NL, I18NUtils.CurrencyFormat.EXACT));

        Assert.assertEquals("1", I18NUtils.formatAmountRound(0.60, LOCALE_NL));
        Assert.assertEquals("0", I18NUtils.formatAmountRound(0.05, LOCALE_NL));
    }

    @Test
    public void formatHumanDateWithinAWeek() {

        Catalog catalog = new Catalog(LOCALE_US);

        DateTime today = new DateTime();

        assertThat(I18NUtils.humanDateFormat(catalog, LOCALE_US, today.toDate())).isEqualTo("today");
        assertThat(I18NUtils.humanDateFormat(catalog, LOCALE_US, today.plusDays(1).toDate())).isEqualTo("tomorrow");
        assertThat(I18NUtils.humanDateFormat(catalog, LOCALE_US, today.minusDays(1).toDate())).isEqualTo("yesterday");

        // Upcoming week, "on Saturday"
        assertThat(I18NUtils.humanDateFormat(catalog, LOCALE_US, today.plusDays(4).toDate())).startsWith("on");

        // Previous week, "last Saturday"
        assertThat(I18NUtils.humanDateFormat(catalog, LOCALE_US, today.minusDays(4).toDate())).startsWith("last");

    }

    @Test
    public void formatHumanDateDifferenceMoreThanAWeek() {

        Catalog catalog = new Catalog(LOCALE_US);

        DateTime today = new DateTime();

        // Within the same year and a date difference from today of at least one week
        DateTime date = today.plusMonths(1).getYear() == today.getYear() ? today.plusMonths(1) : today.minusMonths(1);

        String correctDate = new ThreadSafeDateFormat(catalog.getString("MMM d"), LOCALE_US).format(date.toDate());

        assertThat(I18NUtils.humanDateFormat(catalog, LOCALE_US, date.toDate())).isEqualTo(correctDate);

    }

    @Test
    public void formatHumanDatePreviousYear() {

        Catalog catalog = new Catalog(LOCALE_US);

        // On the previous year
        Date previousYear = new DateTime().minusYears(1).toDate();

        String correctDate = new ThreadSafeDateFormat(catalog.getString("MMM d YYYY"), LOCALE_US).format(previousYear);

        assertThat(I18NUtils.humanDateFormat(catalog, LOCALE_US, previousYear)).isEqualTo(correctDate);
    }

    @Test
    @Parameters({
            "Mon, Mondays",
            "Tue, Tuesdays",
            "Wed, Wednesdays",
            "Thu, Thursdays",
            "Fri, Fridays",
            "Sat, Saturdays",
            "Sun, Sundays"
    })
    public void pluralizedWeekdayNameFromShortName(String shortName, String expectedPluralizedName)
            throws ParseException {
        Catalog catalog = new Catalog(LOCALE_US);
        DayOfWeek dayOfWeek = I18NUtils.getDayOfWeek(LOCALE_US, shortName);
        String pluralized = I18NUtils.getPluralizedWeekdayName(catalog, dayOfWeek);
        assertThat(pluralized).isEqualTo(expectedPluralizedName);
    }
}
