package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankmappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ProviderMappingTest {

    @Test
    public void getAuthenticationTypeByBankCodeShouldThrowExceptionWhenProviderNotFound() {
        // given
        String dummyBankCode = "DUMMY_BANK_CODE";

        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () -> ProviderMapping.getAuthenticationTypeByBankCode(dummyBankCode));

        // then
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void getAuthenticationTypeByBankCodeShouldForNetbank() {
        // given
        String availableCode = ProviderMapping.CULTURA_BANK.getBankCode();

        // when
        AuthenticationType authenticationType =
                ProviderMapping.getAuthenticationTypeByBankCode(availableCode);

        // then
        assertThat(authenticationType).isEqualTo(AuthenticationType.NETTBANK);
    }

    @Test
    public void getAuthenticationTypeByBankCodeShouldForPortalbank() {
        // given
        String availableCode = ProviderMapping.SPAREBANKEN.getBankCode();

        // when
        AuthenticationType authenticationType =
                ProviderMapping.getAuthenticationTypeByBankCode(availableCode);

        // then
        assertThat(authenticationType).isEqualTo(AuthenticationType.PORTAL);
    }
}
