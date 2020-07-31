package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class HISALTest {

    @Test
    public void shouldParseSegmentCorrectlyVersionWithoutIban() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HISAL", "7", "5", "3"},
                    new String[] {"ACCOUNTNUM_98881", "SUBNUM_1294", "COUNTRY_280", "BLZ_70150000"},
                    new String[] {"ACCNAME_12841"},
                    new String[] {"EUR"},
                    new String[] {"C", "6569,44", "EUR", "20200318"},
                    new String[] {"D", "1234,32", "EUR", "20200318"},
                    new String[] {"0,", "EUR"},
                    new String[] {"67182,2", "EUR"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HISAL segment = new HISAL(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HISAL");
        assertThat(segment.getSegmentVersion()).isEqualTo(5);
        assertThat(segment.getSegmentPosition()).isEqualTo(7);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(3);

        assertThat(segment.getIban()).isNull();
        assertThat(segment.getBic()).isNull();
        assertThat(segment.getAccountNumber()).isEqualTo("ACCOUNTNUM_98881");
        assertThat(segment.getSubAccountNumber()).isEqualTo("SUBNUM_1294");
        assertThat(segment.getCountryCode()).isEqualTo("COUNTRY_280");
        assertThat(segment.getBlz()).isEqualTo("BLZ_70150000");

        assertThat(segment.getCurrency()).isEqualTo("EUR");
        assertThat(segment.getFirstBalanceValue()).isEqualTo(BigDecimal.valueOf(6569.44));
        assertThat(segment.getSecondBalanceValue()).isEqualTo(BigDecimal.valueOf(-1234.32));
    }

    @Test
    public void shouldParseSegmentCorrectlyVersionWithIban() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HISAL", "7", "7", "3"},
                    new String[] {
                        "IBAN_12523752",
                        "BIC_92314",
                        "ACCOUNTNUM_98881",
                        "SUBNUM_1294",
                        "COUNTRY_280",
                        "BLZ_70150000"
                    },
                    new String[] {"ACCNAME_12841"},
                    new String[] {"EUR"},
                    new String[] {"C", "6569,44", "EUR", "20200318"},
                    new String[] {"D", "1234,32", "EUR", "20200318"},
                    new String[] {"0,", "EUR"},
                    new String[] {"67182,2", "EUR"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HISAL segment = new HISAL(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HISAL");
        assertThat(segment.getSegmentVersion()).isEqualTo(7);
        assertThat(segment.getSegmentPosition()).isEqualTo(7);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(3);

        assertThat(segment.getIban()).isEqualTo("IBAN_12523752");
        assertThat(segment.getBic()).isEqualTo("BIC_92314");
        assertThat(segment.getAccountNumber()).isEqualTo("ACCOUNTNUM_98881");
        assertThat(segment.getSubAccountNumber()).isEqualTo("SUBNUM_1294");
        assertThat(segment.getCountryCode()).isEqualTo("COUNTRY_280");
        assertThat(segment.getBlz()).isEqualTo("BLZ_70150000");

        assertThat(segment.getCurrency()).isEqualTo("EUR");
        assertThat(segment.getFirstBalanceValue()).isEqualTo(BigDecimal.valueOf(6569.44));
        assertThat(segment.getSecondBalanceValue()).isEqualTo(BigDecimal.valueOf(-1234.32));
    }
}
