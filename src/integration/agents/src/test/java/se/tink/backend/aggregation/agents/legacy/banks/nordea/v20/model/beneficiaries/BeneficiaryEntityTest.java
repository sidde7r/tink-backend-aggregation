package se.tink.backend.aggregation.agents.banks.nordea.v20.model.beneficiaries;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.PrintStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.TestAccount;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;

public class BeneficiaryEntityTest {
    @Test
    public void prepends3300ToPersonkonto() {
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("ThirdParty");
        beneficiaryEntity.setBeneficiaryBankId("NB");
        beneficiaryEntity.setToAccountId("8607015537");

        AccountIdentifier identifier = beneficiaryEntity.generalGetAccountIdentifier();
        assertThat(identifier.is(AccountIdentifier.Type.SE)).isTrue();

        SwedishIdentifier swedishIdentifier = identifier.to(SwedishIdentifier.class);
        assertThat(swedishIdentifier)
                .isEqualTo(AccountIdentifier.create(AccountIdentifier.Type.SE, "33008607015537"));
    }

    @Test
    public void doesNotPrepend3300ToRegularNordeaAccount() {
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("ThirdParty");
        beneficiaryEntity.setBeneficiaryBankId("NB");
        beneficiaryEntity.setToAccountId(TestAccount.NORDEA_EP);

        AccountIdentifier identifier = beneficiaryEntity.generalGetAccountIdentifier();

        AccountIdentifier expected =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.NORDEA_EP);
        assertThat(identifier).isEqualTo(expected);
    }

    @Test
    public void includesClearingForOtherAccount() {
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("ThirdParty");
        beneficiaryEntity.setBeneficiaryBankId("SHB");
        beneficiaryEntity.setToAccountId(TestAccount.HANDELSBANKEN_FH);

        AccountIdentifier identifier = beneficiaryEntity.generalGetAccountIdentifier();

        AccountIdentifier expected =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.HANDELSBANKEN_FH);
        assertThat(identifier).isEqualTo(expected);
    }

    @Test
    public void createSwedishIdentifierForBeneficiaryBankIdOGB() {
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("ThirdParty");
        beneficiaryEntity.setBeneficiaryBankId("OGB");
        beneficiaryEntity.setToAccountId(TestAccount.DANSKEBANK_FH);

        AccountIdentifier identifier = beneficiaryEntity.generalGetAccountIdentifier();
        assertThat(identifier.is(AccountIdentifier.Type.SE)).isTrue();

        SwedishIdentifier swedishIdentifier = identifier.to(SwedishIdentifier.class);

        SwedishIdentifier expected =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.DANSKEBANK_FH)
                        .to(SwedishIdentifier.class);
        assertThat(swedishIdentifier).isEqualTo(expected);
    }

    @Test
    public void createsInvalidIdentifierWhenNordeaBeneficiaryBankIdDoesNotMatchClearing() {
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("ThirdParty");
        beneficiaryEntity.setBeneficiaryBankId("NB");
        beneficiaryEntity.setToAccountId(TestAccount.HANDELSBANKEN_FH);

        AccountIdentifier identifier = beneficiaryEntity.generalGetAccountIdentifier();

        assertThat(identifier).isNotNull();
        assertThat(identifier.isValid()).isFalse();
    }

    @Test
    public void createsInvalidIdentifierWhenGivenInvalidPersonkonto() {
        String validSwedishSSN = "8607015537";
        String invalidCheckDigitSwedishSSN = validSwedishSSN.substring(0, 9) + "0";

        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("ThirdParty");
        beneficiaryEntity.setBeneficiaryBankId("NB");
        beneficiaryEntity.setToAccountId(invalidCheckDigitSwedishSSN);

        AccountIdentifier identifier = beneficiaryEntity.generalGetAccountIdentifier();

        assertThat(identifier).isNotNull();
        assertThat(identifier.isValid()).isFalse();
    }

    @Test
    public void createsInvalidIdentifierForUnknownBeneficiaryBankIdLookup() {
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("ThirdParty");
        beneficiaryEntity.setBeneficiaryBankId("UNKNOWN");
        beneficiaryEntity.setToAccountId(TestAccount.DANSKEBANK_FH);

        AccountIdentifier identifier = beneficiaryEntity.generalGetAccountIdentifier();

        assertThat(identifier).isNotNull();
        assertThat(identifier.isValid()).isFalse();
    }

    @Test
    public void logsErrorForUnknownBeneficiaryBankIdLookup() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        System.setOut(printStream);

        // Create unknown stub
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("ThirdParty");
        beneficiaryEntity.setBeneficiaryBankId("UNKNOWN");
        beneficiaryEntity.setToAccountId(TestAccount.DANSKEBANK_FH);

        // Get identifier (that should output log)
        beneficiaryEntity.generalGetAccountIdentifier();

        // Close stream and read output
        printStream.close();
        byteArrayOutputStream.close();
        String output = new String(byteArrayOutputStream.toByteArray());

        assertThat(output).matches("^(?s).*WARN.*BeneficiaryEntity.*$");
        assertThat(output).contains("UNKNOWN");
        assertThat(output).contains(TestAccount.DANSKEBANK_FH);
    }

    @Test
    public void logsWarningWhenNordeaBeneficiaryBankIdDoesNotMatchClearing() throws IOException {
        String validSwedishSSN = "8607015537";
        String invalidCheckDigitSwedishSSN = validSwedishSSN.substring(0, 9) + "0";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        System.setOut(printStream);

        // Create unknown stub
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("ThirdParty");
        beneficiaryEntity.setBeneficiaryBankId("NB");
        beneficiaryEntity.setToAccountId(invalidCheckDigitSwedishSSN);

        // Get identifier (that should output log)
        beneficiaryEntity.generalGetAccountIdentifier();

        // Close stream and read output
        printStream.close();
        byteArrayOutputStream.close();
        String output = new String(byteArrayOutputStream.toByteArray());

        assertThat(output).matches("^(?s).*WARN.*BeneficiaryEntity.*$");
        assertThat(output).contains("NB");
        assertThat(output).contains(invalidCheckDigitSwedishSSN);
    }

    @Test
    public void bankGiro() {
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("Normal");
        beneficiaryEntity.setPaymentSubTypeExtension("BGType");
        beneficiaryEntity.setBeneficiaryBankId("NB");
        beneficiaryEntity.setToAccountId("1202407");

        AccountIdentifier identifier = beneficiaryEntity.generalGetAccountIdentifier();

        assertThat(identifier.is(AccountIdentifier.Type.SE_BG)).isTrue();
        assertThat(identifier.getIdentifier(new DisplayAccountIdentifierFormatter()))
                .isEqualTo("120-2407");
    }

    @Test
    public void plusGiro() {
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("Normal");
        beneficiaryEntity.setPaymentSubTypeExtension("PGType");
        beneficiaryEntity.setBeneficiaryBankId("NB");
        beneficiaryEntity.setToAccountId("8207037");

        AccountIdentifier identifier = beneficiaryEntity.generalGetAccountIdentifier();

        assertThat(identifier.is(AccountIdentifier.Type.SE_PG)).isTrue();
        assertThat(identifier.getIdentifier(new DisplayAccountIdentifierFormatter()))
                .isEqualTo("820703-7");
    }

    @Test
    public void unkownPaymentSubTypResultsInInvalidIdentifier() {
        BeneficiaryEntity beneficiaryEntity = new BeneficiaryEntity();
        beneficiaryEntity.setPaymentType("Normal");
        beneficiaryEntity.setPaymentSubTypeExtension("OtherType");
        beneficiaryEntity.setBeneficiaryBankId("NB");
        beneficiaryEntity.setToAccountId("8207037");

        AccountIdentifier identifier = beneficiaryEntity.generalGetAccountIdentifier();

        assertThat(identifier).isNotNull();
        assertThat(identifier.isValid()).isFalse();
    }
}
