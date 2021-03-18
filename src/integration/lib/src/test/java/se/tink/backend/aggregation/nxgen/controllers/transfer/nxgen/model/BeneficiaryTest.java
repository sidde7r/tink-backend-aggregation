package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.TinkIdentifier;

public class BeneficiaryTest {

    @Test(expected = NullPointerException.class)
    public void testBuilderWithMissingAccountIdentifier() {
        Beneficiary.builder().withName("foo bar").build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderWithMissingName() throws URISyntaxException {
        Beneficiary.builder()
                .withAccountIdentifier(TinkIdentifier.create(new URI("tink://abc")))
                .build();
    }

    @Test
    public void testCorrectBuilder() throws URISyntaxException {
        Beneficiary beneficiary =
                Beneficiary.builder()
                        .withName("name")
                        .withAccountIdentifier(TinkIdentifier.create(new URI("tink://abc")))
                        .withKeyValue("id", "1")
                        .withKeyValue("foo", "bar")
                        .build();

        assertThat(beneficiary.getName()).isEqualTo("name");
        assertThat(beneficiary.getAccountIdentifier().getIdentifier()).isEqualTo("abc");
        assertThat(beneficiary.getAccountIdentifier().getType())
                .isEqualTo(AccountIdentifierType.TINK);
        assertThat(beneficiary.getValueByKey("id")).isEqualTo("1");
        assertThat(beneficiary.getValueByKey("foo")).isEqualTo("bar");
    }
}
