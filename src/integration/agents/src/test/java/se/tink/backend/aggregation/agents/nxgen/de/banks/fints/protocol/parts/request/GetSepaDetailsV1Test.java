package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class GetSepaDetailsV1Test {

    @Test
    public void shouldSerializeProperly() {
        // given
        BaseRequestPart segment = new GetSepaDetailsV1();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment).isEqualTo("HKSPA:1:1");
    }
}
