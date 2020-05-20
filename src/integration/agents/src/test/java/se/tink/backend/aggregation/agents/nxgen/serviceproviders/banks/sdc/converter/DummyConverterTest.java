package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DummyConverterTest {

    @Test
    public void dummyConverterShouldNotPerformAnyOperationOnGivenInput() {
        // given
        String accountNumber = "PL 12345667890.dummystring y%&^$@#$)(!#@";
        DummyConverter converter = new DummyConverter();

        // when
        String result = converter.convertToIban(accountNumber);

        // then
        assertThat(result).isEqualTo(accountNumber);
    }
}
