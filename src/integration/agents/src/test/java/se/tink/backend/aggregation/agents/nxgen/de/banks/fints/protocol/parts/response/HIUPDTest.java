package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class HIUPDTest {
    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HIUPD", "84", "6", "4"},
                    new String[] {
                        "ACCNUM_1203", "SUBACCNUM_12849", "COUNTRYCODE_280", "BLZ_78060896"
                    },
                    new String[] {"IBAN_DE52783772838689999"},
                    new String[] {"USERID_304756298"},
                    new String[] {"1"},
                    new String[] {"EUR"},
                    new String[] {"ACCHOLDER_001_187489"},
                    new String[] {"ACCHOLDER_002_756475"},
                    new String[] {"PRODUCTNAME_938259"},
                    new String[] {"LIMIT_TYPE", "LIMIT_VALUE", "LIMIT_CURRENCY_EUR", "100"},
                    new String[] {"HKSAK", "1"},
                    new String[] {"HKISA", "1"},
                    new String[] {"HKSSP", "1"},
                    new String[] {"HKCAZ", "1"},
                    new String[] {"HKEKA", "1"},
                    new String[] {"HKIPZ", "1"},
                    new String[] {"HKIPS", "1"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HIUPD segment = new HIUPD(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HIUPD");
        assertThat(segment.getSegmentVersion()).isEqualTo(6);
        assertThat(segment.getSegmentPosition()).isEqualTo(84);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(4);

        assertThat(segment.getAccountNumber()).isEqualTo("ACCNUM_1203");
        assertThat(segment.getSubAccountNumber()).isEqualTo("SUBACCNUM_12849");
        assertThat(segment.getCountryCode()).isEqualTo("COUNTRYCODE_280");
        assertThat(segment.getBlz()).isEqualTo("BLZ_78060896");

        assertThat(segment.getIban()).isEqualTo("IBAN_DE52783772838689999");
        assertThat(segment.getCustomerId()).isEqualTo("USERID_304756298");
        assertThat(segment.getAccountType()).isEqualTo(1);
        assertThat(segment.getCurrencyCode()).isEqualTo("EUR");
        assertThat(segment.getFirstAccountHolder()).isEqualTo("ACCHOLDER_001_187489");
        assertThat(segment.getSecondAccountHolder()).isEqualTo("ACCHOLDER_002_756475");
        assertThat(segment.getProductName()).isEqualTo("PRODUCTNAME_938259");

        assertThat(segment.getAccountLimit())
                .isEqualTo(new HIUPD.Limit("LIMIT_TYPE", "LIMIT_VALUE", "LIMIT_CURRENCY_EUR", 100));

        assertThat(segment.getAllowedBusinessOperations())
                .containsExactly(
                        new HIUPD.AllowedBusinessOperation("HKSAK", 1),
                        new HIUPD.AllowedBusinessOperation("HKISA", 1),
                        new HIUPD.AllowedBusinessOperation("HKSSP", 1),
                        new HIUPD.AllowedBusinessOperation("HKCAZ", 1),
                        new HIUPD.AllowedBusinessOperation("HKEKA", 1),
                        new HIUPD.AllowedBusinessOperation("HKIPZ", 1),
                        new HIUPD.AllowedBusinessOperation("HKIPS", 1));

        assertThat(segment.getAccountAdditionalInfo()).isNull();
    }
}
