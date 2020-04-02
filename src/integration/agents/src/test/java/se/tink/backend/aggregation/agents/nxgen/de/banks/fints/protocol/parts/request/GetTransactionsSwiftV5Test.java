package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import org.junit.Test;

public class GetTransactionsSwiftV5Test {

    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment =
                GetTransactionsSwiftV5.builder()
                        .accountNumber("ACCNUM_123488482")
                        .blz("BLZ_757237")
                        .subAccountNumber("SUB_7727")
                        .startDate(LocalDate.of(1990, 1, 1))
                        .endDate(LocalDate.of(2020, 12, 31))
                        .startingPoint("STPOINT_423")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo(
                        "HKKAZ:1:5+ACCNUM_123488482:SUB_7727:280:BLZ_757237+N+19900101+20201231++STPOINT_423");
    }

    @Test
    public void shouldSerializeProperlyWithBareMinimumFieldsFilled() {
        // given
        BaseRequestPart segment =
                GetTransactionsSwiftV5.builder()
                        .accountNumber("ACCNUM_77182")
                        .blz("BLZ_98786")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment).isEqualTo("HKKAZ:1:5+ACCNUM_77182::280:BLZ_98786+N");
    }

    @Test
    public void shouldThrowNullPointerWhenBuiltWithoutRequiredFields() {
        // given
        GetTransactionsSwiftV5.GetTransactionsSwiftV5Builder builder =
                GetTransactionsSwiftV5.builder()
                        .subAccountNumber("SUB_7234")
                        .startDate(LocalDate.of(1990, 1, 1))
                        .endDate(LocalDate.of(2020, 12, 31))
                        .startingPoint("STPOINT_512");

        // when
        Throwable throwable = catchThrowable(builder::build);

        // then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}
