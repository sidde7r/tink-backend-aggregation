package se.tink.backend.aggregation.agents.nxgen.no.banks.sdc.authenticator.bankmappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankmappers.ProviderMapping;

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
    public void getAuthenticationTypeByBankCodeShouldNotThrowExceptionWhenProviderFound() {
        // given
        String availableCode = ProviderMapping.values()[0].getBankCode();
        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () -> ProviderMapping.getAuthenticationTypeByBankCode(availableCode));
        // then
        assertThat(throwable).isNull();
    }
}
