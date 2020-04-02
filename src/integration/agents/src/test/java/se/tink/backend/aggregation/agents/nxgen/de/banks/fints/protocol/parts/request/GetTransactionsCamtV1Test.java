package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import org.junit.Test;

public class GetTransactionsCamtV1Test {

    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment =
                GetTransactionsCamtV1.builder()
                        .iban("IBAN_346234")
                        .bic("BIC_23488")
                        .accountNumber("ACCNUM_12472199")
                        .subAccountNumber("SUBNUM_75783")
                        .blz("BLZ_35783")
                        .camtFormat("CAMTFORMAT_124124")
                        .startDate(LocalDate.of(2002, 10, 22))
                        .endDate(LocalDate.of(2011, 2, 16))
                        .startingPoint("START_3758")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo(
                        "HKCAZ:1:1+IBAN_346234:BIC_23488:ACCNUM_12472199:SUBNUM_75783:280:BLZ_35783+CAMTFORMAT_124124+N+20021022+20110216++START_3758");
    }

    @Test
    public void shouldSerializeProperlyWithJustRequiredFieldsFilled() {
        // given
        BaseRequestPart segment =
                GetTransactionsCamtV1.builder()
                        .iban("IBAN_8675")
                        .bic("BIC_83334")
                        .accountNumber("ACCNUM_882993")
                        .blz("BLZ_51234")
                        .camtFormat("CAMTFORMAT_9948")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo(
                        "HKCAZ:1:1+IBAN_8675:BIC_83334:ACCNUM_882993::280:BLZ_51234+CAMTFORMAT_9948+N");
    }

    @Test
    public void shouldSerializeProperlyWithoutDatesButWithStartingPoint() {
        // given
        BaseRequestPart segment =
                GetTransactionsCamtV1.builder()
                        .iban("IBAN_1123")
                        .bic("BIC_3344")
                        .accountNumber("ACCNUM_5522")
                        .blz("BLZ_12673")
                        .camtFormat("CAMTFORMAT_7656")
                        .startingPoint("START_1199775")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo(
                        "HKCAZ:1:1+IBAN_1123:BIC_3344:ACCNUM_5522::280:BLZ_12673+CAMTFORMAT_7656+N++++START_1199775");
    }

    @Test
    public void shouldSerializeProperlyWithDatesButWithoutStartingPoint() {
        // given
        BaseRequestPart segment =
                GetTransactionsCamtV1.builder()
                        .iban("IBAN_1123")
                        .bic("BIC_3344")
                        .accountNumber("ACCNUM_5522")
                        .blz("BLZ_12673")
                        .camtFormat("CAMTFORMAT_7656")
                        .startDate(LocalDate.of(2011, 7, 2))
                        .endDate(LocalDate.of(2019, 8, 6))
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo(
                        "HKCAZ:1:1+IBAN_1123:BIC_3344:ACCNUM_5522::280:BLZ_12673+CAMTFORMAT_7656+N+20110702+20190806");
    }

    @Test
    public void shouldThrowNullPointerWhenBuiltWithoutRequiredFields() {
        // given
        GetTransactionsCamtV1.GetTransactionsCamtV1Builder builder =
                GetTransactionsCamtV1.builder()
                        .subAccountNumber("SUBNUM_75783")
                        .startDate(LocalDate.of(2002, 10, 22))
                        .endDate(LocalDate.of(2011, 2, 16))
                        .startingPoint("START_3758");

        // when
        Throwable throwable = catchThrowable(builder::build);

        // then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}
