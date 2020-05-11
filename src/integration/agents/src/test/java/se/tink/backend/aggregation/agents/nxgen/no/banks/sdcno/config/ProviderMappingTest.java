package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config;

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
                        () -> ProviderMapping.getProviderMappingTypeByBankCode(dummyBankCode));

        // then
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void getAuthenticationTypeByBankCodeShouldForNetbank() {
        // given
        String availableCode = ProviderMapping.CULTURA_BANK.getBankCode();

        // when
        ProviderMapping providerMapping =
                ProviderMapping.getProviderMappingTypeByBankCode(availableCode);
        AuthenticationType authenticationType = providerMapping.getAuthenticationType();

        // then
        assertThat(authenticationType).isEqualTo(AuthenticationType.NETTBANK);
    }

    @Test
    public void getAuthenticationTypeByBankCodeShouldForPortalbank() {
        // given
        String availableCode = ProviderMapping.SPAREBANKEN.getBankCode();

        // when
        ProviderMapping providerMapping =
                ProviderMapping.getProviderMappingTypeByBankCode(availableCode);
        AuthenticationType authenticationType = providerMapping.getAuthenticationType();

        // then
        assertThat(authenticationType).isEqualTo(AuthenticationType.PORTAL);
    }
}
