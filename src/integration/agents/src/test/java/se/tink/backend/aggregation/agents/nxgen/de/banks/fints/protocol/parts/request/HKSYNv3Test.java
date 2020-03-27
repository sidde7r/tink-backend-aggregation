package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class HKSYNv3Test {

    @Test
    public void shouldSerializeProperlyWithConstantValues() {
        // given
        BaseRequestPart segment = new HKSYNv3();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment).isEqualTo("HKSYN:1:3+0");
    }
}
