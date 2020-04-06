package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PayloadParserTest {

    @Test
    public void shouldGetProperlyParsedPayload() {
        PayloadParser.Payload payload = PayloadParser.parse("12345 http://bar.com/bar blaaaaa");
        assertThat(payload.getBankName()).isEqualTo("blaaaaa");
        assertThat(payload.getEndpoint()).isEqualTo("http://bar.com/bar");
        assertThat(payload.getBlz()).isEqualTo("12345");
    }
}
