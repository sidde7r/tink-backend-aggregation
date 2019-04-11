package se.tink.backend.aggregation.agents.banks.danskebank;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.TestAccount;
import se.tink.libraries.date.DateUtils;

public class DanskeUtilsTest {
    @Test(expected = IllegalStateException.class)
    public void testBankLookupForUnsupportedAccountIdentifier_ShouldThrow() {
        IbanIdentifier tinkIdentifier =
                new IbanIdentifier("SWEDSESS", "SE28 8000 0832 7900 0001 2345");

        DanskeUtils.getBankId(tinkIdentifier);
    }

    @Test
    public void testBankLookupSwedbank() {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(TestAccount.SWEDBANK_FH);

        String bankId = DanskeUtils.getBankId(swedishIdentifier);

        assertThat(bankId).isEqualTo("8000SWEDBANK");
    }

    @Test
    public void testBankLookupSavingsbank() {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(TestAccount.SAVINGSBANK_AL);

        String bankId = DanskeUtils.getBankId(swedishIdentifier);

        assertThat(bankId).isEqualTo("8000SWEDBANK");
    }

    @Test
    public void testBankLookupNordea() {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(TestAccount.NORDEA_EP);

        String bankId = DanskeUtils.getBankId(swedishIdentifier);

        assertThat(bankId).isEqualTo("3000NORDEA");
    }

    @Test
    public void testBankLookupNordeaPersonkonto() {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(TestAccount.NORDEASSN_EP);

        String bankId = DanskeUtils.getBankId(swedishIdentifier);

        assertThat(bankId).isEqualTo("3333NORDEA - PERSONKONTON");
    }

    @Test
    public void testBankLookupLansforsakringar() {
        SwedishIdentifier swedishIdentifier =
                new SwedishIdentifier(TestAccount.LANSFORSAKRINGAR_FH);

        String bankId = DanskeUtils.getBankId(swedishIdentifier);

        assertThat(bankId).isEqualTo("9020LÄNSFÖRSÄKRINGAR BANK AB");
    }

    @Test
    public void testBankLookupDanskebank() {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(TestAccount.DANSKEBANK_FH);

        String bankId = DanskeUtils.getBankId(swedishIdentifier);

        assertThat(bankId).isEqualTo("1200DANSKE BANK SVERIGE");
    }

    @Test
    public void testBankLookupSkandiabanken() {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(TestAccount.SKANDIABANKEN_FH);

        String bankId = DanskeUtils.getBankId(swedishIdentifier);

        assertThat(bankId).isEqualTo("9150SKANDIABANKEN");
    }

    @Test
    public void testBankLookupHandelsbanken() {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(TestAccount.HANDELSBANKEN_FH);

        String bankId = DanskeUtils.getBankId(swedishIdentifier);

        assertThat(bankId).isEqualTo("6000HANDELSBANKEN");
    }

    @Test
    public void testBankLookupIcabanken() {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(TestAccount.ICABANKEN_FH);

        String bankId = DanskeUtils.getBankId(swedishIdentifier);

        assertThat(bankId).isEqualTo("9270ICA BANKEN");
    }

    @Test
    public void testBankLookupSEB() {
        SwedishIdentifier swedishIdentifier = new SwedishIdentifier(TestAccount.SEB_DL);

        String bankId = DanskeUtils.getBankId(swedishIdentifier);

        assertThat(bankId).isEqualTo("5000SEB");
    }

    @Test
    public void parsesDateFromDanskeFormat() {
        String danskeDateTime = "/Date(1464220800000+0200)/";

        Date date =
                new DateTime()
                        .withYear(2016)
                        .withMonthOfYear(5)
                        .withDayOfMonth(26)
                        .withMillisOfDay(0)
                        .toDate();
        Date flattenedDate = DateUtils.flattenTime(date);

        assertThat(DanskeUtils.parseDanskeDate(danskeDateTime)).isEqualTo(flattenedDate);
    }

    @Test
    public void testFormatDate() {
        // Based on app traffic, comparing "\/Date(1464220800000+0200)\/" in request to response:
        // "Betalningsdatum": "2016-05-26"
        DateTime dateTime =
                new DateTime()
                        .withYear(2016)
                        .withMonthOfYear(5)
                        .withDayOfMonth(26)
                        .withMillisOfDay(0)
                        .withZone(DateTimeZone.forOffsetHours(2));

        String formattedDate = DanskeUtils.formatDanskeDateDaily(dateTime.toDate());
        assertThat(formattedDate).isEqualTo("\\/Date(1464220800000+0200)\\/");
    }
}
