package se.tink.backend.categorization.rules;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import se.tink.backend.categorization.api.AbnAmroCategories;
import static org.assertj.core.api.Assertions.assertThat;

public class AbnAmroCategorizationCommandTest {

    @Test
    public void testExpensePatterns() {

        ImmutableMap<String, Pattern> patterns = AbnAmroCategorizationCommand.getExpensePatterns();

        // Verify a non matching pattern
        assertThat(match(patterns, "Should not match")).isNull();

        // Verify matching patterns
        assertThat(match(patterns, "ABN AMRO BANK PRIVEPAKKET")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_HOME_OTHER);
        assertThat(match(patterns, "ABN AMRO BANK REKENING")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_HOME_OTHER);
        assertThat(match(patterns, "ABN AMRO BANK")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_HOME_MORTGAGE);
        assertThat(match(patterns, "ABN AMRO VERZ")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES);
        assertThat(match(patterns, "ABN AMRO BANK BETAALGEMAK")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_HOME_OTHER);
        assertThat(match(patterns, "ABN AMRO BELEGGEN")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_HOME_OTHER);
        assertThat(match(patterns, "ABNAMRO BELEGGEN")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_HOME_OTHER);
        assertThat(match(patterns, "ABN AMRO BETAALPAS")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_HOME_OTHER);

        // Verify matching Tikkie patterns
        assertThat(match(patterns, "ABN AMRO BANK INZ TIKKIE")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_MISC_OUTLAYS);
        assertThat(match(patterns, "Tikkie - Kalle Anka")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_MISC_OUTLAYS);
    }

    @Test
    public void testComplicatedExpensePatterns() {
        ImmutableMap<String, Pattern> patterns = AbnAmroCategorizationCommand.getExpensePatterns();

        // The Dutch Railway
        assertThat(match(patterns, "NS-Amsterdam")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_PUBLICTRANSPORT);
        assertThat(match(patterns, "Rotterdam NS-Foo")).isNull();

        // McDonalds (should match both McDonalds and McD, but not everything that starts with McD)
        assertThat(match(patterns, "McD Amsterdam")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS);
        assertThat(match(patterns, "McDonalds 5005 ROTTERDAM")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS);
        assertThat(match(patterns, "McDonnell Douglas")).isNull();

        // Eye Wish
        assertThat(match(patterns, "Eye Wish AMS")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_WELLNESS_EYECARE);
        assertThat(match(patterns, "Eyewish De Haag")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_WELLNESS_EYECARE);

        // H&M
        assertThat(match(patterns, "H&M Rotterdam")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES);
        assertThat(match(patterns, "HM.COM Stockholm")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES);

        // Bristol (clothing store)
        assertThat(match(patterns, "BRISTOL BAGGAGE RECLAI")).isNull();
        assertThat(match(patterns, "Bristol 8014 Rotterd ROT")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES);

        // Boni & Bonita
        assertThat(match(patterns, "Boni")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_FOOD_GROCERIES);
        assertThat(match(patterns, "Bonita")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES);

        // Simon Levelt
        assertThat(match(patterns, "S.Levelt")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_FOOD_COFFEE);
        assertThat(match(patterns, "S Levelt")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_FOOD_COFFEE);
        assertThat(match(patterns, "Simon Levelt")).isEqualTo(AbnAmroCategories.Codes.EXPENSES_FOOD_COFFEE);
    }

    @Test
    public void testIncomePatterns() {
        ImmutableMap<String, Pattern> patterns = AbnAmroCategorizationCommand.getIncomePatterns();

        assertThat(match(patterns, "Should not match")).isNull();

        assertThat(match(patterns, "ABN AMRO BANK")).isEqualTo(AbnAmroCategories.Codes.INCOME_SALARY_OTHER);
        assertThat(match(patterns, "ABN AMRO VERZ")).isEqualTo(AbnAmroCategories.Codes.INCOME_OTHER_OTHER);
        assertThat(match(patterns, "Tikkie - Roger Moore")).isEqualTo(AbnAmroCategories.Codes.INCOME_REFUND_OTHER);
    }

    private String match(ImmutableMap<String, Pattern> rules, String description) {
        for (Map.Entry<String, Pattern> rule : rules.entrySet()) {
            Matcher m = rule.getValue().matcher(description);
            if (m.find()) {
                return rule.getKey();
            }
        }
        return null;
    }

}
