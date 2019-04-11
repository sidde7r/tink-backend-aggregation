package se.tink.backend.aggregation.agents.banks.seb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.TestAccount;

public class SebAccountIdentifierFormatterTest {

    private final SebAccountIdentifierFormatter formatter = new SebAccountIdentifierFormatter();

    @Test
    public void testNordeaFormatting() {
        Assert.assertEquals(
                "8401141935", new SwedishIdentifier("33008401141935").getIdentifier(formatter));
    }

    @Test
    public void testSavingsbankFormatting() {
        Assert.assertEquals(
                "842280031270465", new SwedishIdentifier("8422831270465").getIdentifier(formatter));
    }

    @Test
    public void parseNordeaSSNAccount() {
        SebAccountIdentifierFormatter formatter = new SebAccountIdentifierFormatter();
        Optional<SwedishIdentifier> parsedIdentifier =
                formatter.parseSwedishIdentifier("8401141935", "NB");

        assertThat(parsedIdentifier.isPresent()).isTrue();
        assertThat(parsedIdentifier.get())
                .isEqualTo(new SwedishIdentifier(TestAccount.NORDEASSN_EP));
    }

    @Test
    public void parseNordeaAccount() {
        String accountNumber = TestAccount.NORDEA_EP;

        SebAccountIdentifierFormatter formatter = new SebAccountIdentifierFormatter();
        Optional<SwedishIdentifier> parsedIdentifier =
                formatter.parseSwedishIdentifier(accountNumber, "NB");

        assertThat(parsedIdentifier.isPresent()).isTrue();
        assertThat(parsedIdentifier.get()).isEqualTo(new SwedishIdentifier(accountNumber));
    }

    @Test
    public void parseOtherAccountWithNordeaPrefixIsNotPresent() {
        String accountNumber = TestAccount.SKANDIABANKEN_FH;

        SebAccountIdentifierFormatter formatter = new SebAccountIdentifierFormatter();
        Optional<SwedishIdentifier> parsedIdentifier =
                formatter.parseSwedishIdentifier(accountNumber, "NB");

        assertThat(parsedIdentifier.isPresent()).isFalse();
    }

    @Test
    public void parseOtherAccount() {
        String accountNumber = TestAccount.DANSKEBANK_FH;

        SebAccountIdentifierFormatter formatter = new SebAccountIdentifierFormatter();
        Optional<SwedishIdentifier> parsedIdentifier =
                formatter.parseSwedishIdentifier(accountNumber, null);

        assertThat(parsedIdentifier.isPresent()).isTrue();
        assertThat(parsedIdentifier.get()).isEqualTo(new SwedishIdentifier(accountNumber));
    }

    @Test
    public void parseInternalAccount() {
        String accountNumber = TestAccount.SEB_DL;

        SebAccountIdentifierFormatter formatter = new SebAccountIdentifierFormatter();
        Optional<SwedishIdentifier> parsedIdentifier =
                formatter.parseInternalIdentifier(accountNumber);

        assertThat(parsedIdentifier.isPresent()).isTrue();
        assertThat(parsedIdentifier.get()).isEqualTo(new SwedishIdentifier(accountNumber));
    }

    @Test
    public void parseInternalAccountThatIsNotSEBIsNotPresent() {
        String accountNumber = TestAccount.SKANDIABANKEN_FH;

        SebAccountIdentifierFormatter formatter = new SebAccountIdentifierFormatter();
        Optional<SwedishIdentifier> parsedIdentifier =
                formatter.parseInternalIdentifier(accountNumber);

        assertThat(parsedIdentifier.isPresent()).isFalse();
    }
}
