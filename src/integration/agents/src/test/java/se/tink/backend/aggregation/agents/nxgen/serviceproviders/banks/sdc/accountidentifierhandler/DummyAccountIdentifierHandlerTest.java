package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.accountidentifierhandler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DummyAccountIdentifierHandlerTest {

    @Test
    public void dummyIdentifierShouldNotPerformAnyOperationOnGivenInput() {
        // given
        String accountNumber = "PL 12345667890.dummystring y%&^$@#$)(!#@";
        DummyAccountIdentifierHandler converter = new DummyAccountIdentifierHandler();

        // when
        String result = converter.convertToIban(accountNumber);

        // then
        assertThat(result).isEqualTo(accountNumber);
    }
}
