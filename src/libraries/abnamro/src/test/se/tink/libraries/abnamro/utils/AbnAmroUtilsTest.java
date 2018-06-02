package se.tink.libraries.abnamro.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class AbnAmroUtilsTest {

    @Test
    public void testDescriptionPartsParsing() {

        List<String> descriptionLines;
        Map<String, String> descriptionParts;

        // POS card payment (with POS device id number).

        descriptionLines = Lists.newArrayList(
                "BEA   NR:9X5XKY   20.12.14/12.26",
                "Amigo Amersfoort AMERSFO,PAS363 ");

        descriptionParts = AbnAmroUtils.getDescriptionParts(descriptionLines);

        Assert.assertEquals("Amigo Amersfoort AMERSFO,PAS363", descriptionParts.get(AbnAmroUtils.DescriptionKeys.POS));

        // POS card payment (without POS device id number; outside of Netherlands).

        descriptionLines = Lists.newArrayList(
                "BEA               18.06.14/15.51",
                "STE BOPREDIS ST CYPRIEN ,PAS352 ");

        descriptionParts = AbnAmroUtils.getDescriptionParts(descriptionLines);

        Assert.assertEquals("STE BOPREDIS ST CYPRIEN ,PAS352", descriptionParts.get(AbnAmroUtils.DescriptionKeys.POS));

        // ATM cash withdrawal (with ATM id number; outside of Netherlands).

        descriptionLines = Lists.newArrayList(
                "GEA   NR:00063613 08.03.15/09.35",
                "RAIFFEISENBANK SERFAUS S,PAS228 ");

        descriptionParts = AbnAmroUtils.getDescriptionParts(descriptionLines);

        Assert.assertEquals("RAIFFEISENBANK SERFAUS S,PAS228", descriptionParts.get(AbnAmroUtils.DescriptionKeys.POS));

        // ATM cash withdrawal (without ATM id number; outside of Netherlands).

        descriptionLines = Lists.newArrayList(
                "GEA               21.06.14/10.02",
                "SARLAT DAB 1,PAS352             ");

        descriptionParts = AbnAmroUtils.getDescriptionParts(descriptionLines);

        Assert.assertEquals("SARLAT DAB 1,PAS352", descriptionParts.get(AbnAmroUtils.DescriptionKeys.POS));

        // Direct debit.

        descriptionLines = Lists.newArrayList(
                "SEPA Incasso algemeen doorlopend",
                "Incassant: NL03ZZZ301771260000  ",
                "Naam: TLS BV inzake OV-Chipkaart",
                "Machtiging: NL03ZZZ3017712600005",
                "690011003049235                 ",
                "Omschrijving: Automatisch oplade",
                "n OV-chipkaart Kaartnummer:35280",
                "10461984490 Oplaaddatum/tijd:26-",
                "08-15 07:36                     ");

        descriptionParts = AbnAmroUtils.getDescriptionParts(descriptionLines);

        Assert.assertEquals("TLS BV inzake OV-Chipkaart", descriptionParts.get(AbnAmroUtils.DescriptionKeys.NAME));
        Assert.assertEquals(
                "Automatisch oplade n OV-chipkaart",
                // Desired: "Automatisch opladen OV-chipkaart",
                descriptionParts.get(AbnAmroUtils.DescriptionKeys.DESCRIPTION));

        // Online payment.

        descriptionLines = Lists.newArrayList(
                "SEPA iDEAL                      ",
                "IBAN: NL21INGB0674773837        ",
                "BIC: INGBNL2A                   ",
                "Naam: ING Bank                  ",
                "Omschrijving: LWIALSVW4BADDIFHNX",
                "BVA7646M 0050002051358474 4 sche",
                "rmen tegelijk Netflix.com       ",
                "Kenmerk: 24-10-2015 12:35 005000",
                "2051358474                      ");

        descriptionParts = AbnAmroUtils.getDescriptionParts(descriptionLines);

        Assert.assertEquals("ING Bank", descriptionParts.get(AbnAmroUtils.DescriptionKeys.NAME));
        Assert.assertEquals(
                "LWIALSVW4BADDIFHNX BVA7646M 0050002051358474 4 sche rmen tegelijk Netflix.com",
                // Desired: "LWIALSVW4BADDIFHNXBVA7646M 0050002051358474 4 schermen tegelijk Netflix.com",
                descriptionParts.get(AbnAmroUtils.DescriptionKeys.DESCRIPTION));

        // Transfer.

        descriptionLines = Lists.newArrayList(
                "SEPA Overboeking                ",
                "IBAN: NL27INGB0005750858        ",
                "BIC: INGBNL2A                   ",
                "Naam: Sd manten                 ",
                "Omschrijving: De Woelf, je weet ",
                "toch..!                         ");

        descriptionParts = AbnAmroUtils.getDescriptionParts(descriptionLines);

        Assert.assertEquals("Sd manten", descriptionParts.get(AbnAmroUtils.DescriptionKeys.NAME));
        Assert.assertEquals("De Woelf, je weet toch..!",
                descriptionParts.get(AbnAmroUtils.DescriptionKeys.DESCRIPTION));

    }

    @Test
    public void noFieldsSetShouldNotBeDuplicate() {
        Transaction left = new Transaction();
        Transaction right = new Transaction();

        assertThat(AbnAmroUtils.isDuplicate(left, right)).isFalse();
    }

    @Test
    public void differentAccountsShouldNotBeDuplicates() {
        Transaction left = new Transaction();
        left.setAccountId("account1");
        left.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "1");

        Transaction right = new Transaction();
        right.setAccountId("account2");
        right.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "1");

        assertThat(AbnAmroUtils.isDuplicate(left, right)).isFalse();
    }

    @Test
    public void sameAccountAndExternalIdShouldBeDuplicate() {
        Transaction left = new Transaction();
        left.setAccountId("account1");
        left.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "1");

        Transaction right = new Transaction();
        right.setAccountId("account1");
        right.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "1");

        assertThat(AbnAmroUtils.isDuplicate(left, right)).isTrue();
    }

    @Test
    public void testMaskAccountNumber() {
        assertThat(AbnAmroUtils.maskCreditCardContractNumber("1234567891111234")).isEqualTo("**** **** **** 1234");
        assertThat(AbnAmroUtils.maskCreditCardContractNumber("1234")).isEqualTo("1234");
        assertThat(AbnAmroUtils.maskCreditCardContractNumber("**** **** **** 1234")).isEqualTo("**** **** **** 1234");
    }

    @Test
    public void noNewAccountsShouldNotBeMatched() {

        List<Account> existingAccounts = ImmutableList.of();
        List<Account> availableAccounts = ImmutableList.of();

        assertThat(AbnAmroUtils.getAccountDifference(availableAccounts, existingAccounts)).isEmpty();
    }

    @Test
    public void newAccountWithSameBankIdShouldNotBeMatched() {

        Account existingAccount = new Account();
        existingAccount.setBankId("1");

        Account newAccount = new Account();
        newAccount.setBankId("1");

        List<Account> existingAccounts = ImmutableList.of(existingAccount);
        List<Account> availableAccounts = ImmutableList.of(newAccount);

        assertThat(AbnAmroUtils.getAccountDifference(availableAccounts, existingAccounts)).isEmpty();
    }

    @Test
    public void newAccountWithDifferentBankIdShouldNotBeMatched() {

        Account existingAccount = new Account();
        existingAccount.setBankId("1");

        Account newAccount = new Account();
        newAccount.setBankId("2");

        List<Account> existingAccounts = ImmutableList.of(existingAccount);
        List<Account> availableAccounts = ImmutableList.of(newAccount);

        assertThat(AbnAmroUtils.getAccountDifference(availableAccounts, existingAccounts)).containsExactly(newAccount);
    }

    @Test
    public void existingAccountWithDifferentBankIdShouldNotBeMatched() {

        Account existingAccount1 = new Account();
        existingAccount1.setBankId("1");

        Account existingAccount2 = new Account();
        existingAccount2.setBankId("2");

        Account newAccount1 = new Account();
        newAccount1.setBankId("1");

        Account newAccount2 = new Account();
        newAccount2.setBankId("4");

        List<Account> availableAccounts = ImmutableList.of(newAccount1, newAccount2); // 1, 4
        List<Account> existingAccounts = ImmutableList.of(existingAccount1, existingAccount2); // 1, 2

        assertThat(AbnAmroUtils.getAccountDifference(availableAccounts, existingAccounts)).containsExactly(newAccount2);
    }

    @Test
    public void testAbnAmroProviderMatching() {
        assertThat(AbnAmroUtils.isAbnAmroProvider("nordea")).isFalse();
        assertThat(AbnAmroUtils.isAbnAmroProvider(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME)).isTrue();
        assertThat(AbnAmroUtils.isAbnAmroProvider(AbnAmroUtils.ABN_AMRO_ICS_PROVIDER_NAME)).isTrue();
    }

    @Test
    public void testNonRejectedAccount() {
        Account account = new Account();
        assertThat(AbnAmroUtils.isAccountRejected(account)).isFalse();
    }

    @Test
    public void testRejectedAccount() {
        Account account = new Account();
        AbnAmroUtils.markAccountAsRejected(account, 123);
        assertThat(AbnAmroUtils.isAccountRejected(account)).isTrue();
    }

    @Test
    public void testActivatingRejectedAccount() {
        Account account = new Account();
        AbnAmroUtils.markAccountAsRejected(account, 123);
        assertThat(AbnAmroUtils.isAccountRejected(account)).isTrue();

        AbnAmroUtils.markAccountAsActive(account);
        assertThat(AbnAmroUtils.isAccountRejected(account)).isFalse();
    }

    @Test
    @Parameters({
            "en_US",
            "en_us",
            "nl_NL",
            "nl_nl"
    })
    public void testValidLocales(String locale) {
        assertThat(AbnAmroUtils.isValidLocale(locale)).isTrue();
    }

    @Test
    @Parameters({
            "en_GB",
            "nl_BE",
    })
    public void testInvalidLocales(String locale) {
        assertThat(AbnAmroUtils.isValidLocale(locale)).isFalse();
    }

    @Test
    public void testNullLocale() {
        assertThat(AbnAmroUtils.isValidLocale(null)).isFalse();
    }

    @Test
    public void testPrettyFormatIban() {
        assertThat(AbnAmroUtils.prettyFormatIban(null)).isNull();
        assertThat(AbnAmroUtils.prettyFormatIban("NL91ABNA0000000000")).isEqualTo("NL91 ABNA 0000 0000 00");
        assertThat(AbnAmroUtils.prettyFormatIban("NL91ABNA000000000")).isEqualTo("NL91 ABNA 0000 0000 0");
        assertThat(AbnAmroUtils.prettyFormatIban("NL91ABNA00000000")).isEqualTo("NL91 ABNA 0000 0000");
        assertThat(AbnAmroUtils.prettyFormatIban("NL91ABNA0000000")).isEqualTo("NL91 ABNA 0000 000");
    }

    @Test
    public void testInvalidBcNumbersFormats() {
        assertThat(AbnAmroUtils.isValidBcNumberFormat(null)).isFalse();
        assertThat(AbnAmroUtils.isValidBcNumberFormat("")).isFalse();
        assertThat(AbnAmroUtils.isValidBcNumberFormat("BLOCKED")).isFalse();
        assertThat(AbnAmroUtils.isValidBcNumberFormat("A")).isFalse();
        assertThat(AbnAmroUtils.isValidBcNumberFormat("BC1")).isFalse();
    }

    @Test
    public void testValidBcNumbersFormats() {
        assertThat(AbnAmroUtils.isValidBcNumberFormat("1")).isTrue();
        assertThat(AbnAmroUtils.isValidBcNumberFormat("12")).isTrue();
        assertThat(AbnAmroUtils.isValidBcNumberFormat("01212")).isTrue();
    }

    @Test
    public void testNoBcNumberWhenNull() {
        assertThat(AbnAmroUtils.getBcNumber(null).isPresent()).isFalse();
    }

    @Test
    public void testNoBcNumberWhenWrongProvider() {
        Credentials credentials = new Credentials();
        credentials.setProviderName("nordea-bankid");

        assertThat(AbnAmroUtils.getBcNumber(credentials).isPresent()).isFalse();
    }

    @Test
    public void testNoBcNumberWhenBlocked() {
        Credentials credentials = new Credentials();
        credentials.setProviderName(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME_V2);
        credentials.setPayload("BLOCKED");

        assertThat(AbnAmroUtils.getBcNumber(credentials).isPresent()).isFalse();
    }

    @Test
    public void testGetCorrectBcNumber() {
        Credentials credentials = new Credentials();
        credentials.setProviderName(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME_V2);
        credentials.setPayload("12345");

        assertThat(AbnAmroUtils.getBcNumber(credentials).orElse(null)).isEqualTo("12345");
    }

}
