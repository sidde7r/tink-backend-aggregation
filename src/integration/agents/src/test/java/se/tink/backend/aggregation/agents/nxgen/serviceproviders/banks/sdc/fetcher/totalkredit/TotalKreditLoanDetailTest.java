package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class TotalKreditLoanDetailTest {

    @Test
    public void value() {
        // given
        TotalKreditLoanDetail detail =
                SerializationUtils.deserializeFromString(INPUT_DATA, TotalKreditLoanDetail.class);

        // when
        String result = detail.value();

        // then
        assertThat(result).isEqualTo("sample value");
    }

    @Test
    @Parameters(method = "detailsArrayForIs")
    public void is(final String desiredLabel, final boolean expectedResult) {
        // given
        TotalKreditLoanDetail detail =
                SerializationUtils.deserializeFromString(INPUT_DATA, TotalKreditLoanDetail.class);

        // when
        boolean result = detail.is(desiredLabel);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    @Parameters(method = "detailsArrayForIsSimilar")
    public void isSimilar(final String desiredLabel, final boolean expectedResult) {
        // given
        TotalKreditLoanDetail detail =
                SerializationUtils.deserializeFromString(INPUT_DATA, TotalKreditLoanDetail.class);

        // when
        boolean result = detail.isSimilar(desiredLabel);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    private static final String INPUT_DATA =
            "{\"label\": \"sample label\", \"value\": \"sample value\"}";

    private Object[] detailsArrayForIs() {
        return new Object[] {
            new Object[] {"not matching desired label", false},
            new Object[] {"", false},
            new Object[] {null, false},
            new Object[] {"sample label", true}
        };
    }

    private Object[] detailsArrayForIsSimilar() {
        return new Object[] {
            new Object[] {"not matching desired label", false},
            new Object[] {"sample", true},
            new Object[] {"sample label", true}
        };
    }
}
