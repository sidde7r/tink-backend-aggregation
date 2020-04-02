package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;

public class AuthorizeV2Test {

    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment =
                AuthorizeV2.builder()
                        .blz("BLZ_123987")
                        .systemId("SYSTEMID_9911228833")
                        .username("AmazingUser123")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo("HKIDN:1:2+280:BLZ_123987+AmazingUser123+SYSTEMID_9911228833+1");
    }

    @Test
    public void shouldThrowNullPointerWhenBuiltWithoutRequiredFields() {
        // given
        AuthorizeV2.AuthorizeV2Builder builder = AuthorizeV2.builder();

        // when
        Throwable throwable = catchThrowable(builder::build);

        // then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}
