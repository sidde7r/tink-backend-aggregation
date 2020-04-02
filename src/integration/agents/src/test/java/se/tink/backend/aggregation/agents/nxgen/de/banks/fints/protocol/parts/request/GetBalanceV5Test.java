package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;

public class GetBalanceV5Test {

    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment =
                GetBalanceV5.builder()
                        .accountNumber("ACCNUM_9248")
                        .subAccountNumber("SUBNUM_1652")
                        .blz("BLZ_124214")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo("HKSAL:1:5+ACCNUM_9248:SUBNUM_1652:280:BLZ_124214+N");
    }

    @Test
    public void shouldThrowNullPointerWhenBuiltWithoutRequiredFields() {
        // given
        GetBalanceV5.GetBalanceV5Builder builder =
                GetBalanceV5.builder().subAccountNumber("SUBNUM_88662");

        // when
        Throwable throwable = catchThrowable(builder::build);

        // then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}
