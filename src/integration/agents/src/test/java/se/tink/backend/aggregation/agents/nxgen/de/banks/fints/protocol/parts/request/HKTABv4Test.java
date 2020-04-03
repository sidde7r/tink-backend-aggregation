package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class HKTABv4Test {

    @Test
    public void shouldSerializeProperlyWithConstantValues() {
        // given
        BaseRequestPart segment = new HKTABv4();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment).isEqualTo("HKTAB:1:4+0+A");
    }
}
