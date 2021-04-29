package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class WizinkCardUtilTest {

    @Test
    public void shouldReturnEncodedAndMaskedCardNumberWhenNumberReturnedByBankIsMasked() {
        // given
        String maskedCardNumber =
                "ZGE2ZTM4ZjBiNjhjMGQ2ODViMjE5NzRiZmIyNmE2NjgwYWNjOWI3YWJiOGI0NzQzN2JkNjA4Mzg0MTI2NzgwYg\\u003d\\u003d";
        String xTokenUserHeader =
                "DA6E38F0B68C0D685B21974BFB0C8C4220ECB15091A1676951FC221878114139";

        // when
        String decodedMaskedCardNumber =
                WizinkCardUtil.getMaskedCardNumber(maskedCardNumber, xTokenUserHeader);

        // then
        assertThat(decodedMaskedCardNumber).isEqualTo("**** **** **** 9792");
    }

    @Test
    public void shouldReturEncodedAndMaskedCardNumberWhenNumberReturnedByBankIsUnmasked() {
        // given
        String unmaskedCardNumber =
                "ZGE2ZTM4ZjBiNjhjMGQ2ODViMjE5NzRiZmIzOGI1NzIxOWNjODU2M2E2OTM0NzU5NjljZTE1Mzg0MTI2NzgwYg\\u003d\\u003d";
        String xTokenUserHeader =
                "DA6E38F0B68C0D685B21974BFB0C8C4220ECB15091A1676951FC221878114139";

        // when
        String decodedMaskedCardNumber =
                WizinkCardUtil.getMaskedCardNumber(unmaskedCardNumber, xTokenUserHeader);

        // then
        assertThat(decodedMaskedCardNumber).isEqualTo("**** **** **** 9792");
    }
}
