package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CbiUrlUtilsTest {

    @Test
    public void shouldEncodeQueryParamSpecialSigns() {
        // when
        String encondedRedirectURIQueryParams =
                CbiUrlUtils.getEncondedRedirectURIQueryParams("TEST_STATE", "TEST_RESULT");

        // then
        assertThat(encondedRedirectURIQueryParams)
                .isEqualTo("%3Fstate%3DTEST_STATE%26result%3DTEST_RESULT");
    }

    @Test
    public void shouldEncodeSpacesInString() {
        // when
        String valueWithEncededSpaces = CbiUrlUtils.encodeBlankSpaces("one two three  four");

        // then
        assertThat(valueWithEncededSpaces).isEqualTo("one%20two%20three%20%20four");
    }
}
