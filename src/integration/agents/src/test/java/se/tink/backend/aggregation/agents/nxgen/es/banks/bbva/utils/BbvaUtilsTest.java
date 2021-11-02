package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.vavr.control.Option;
import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class BbvaUtilsTest {

    private static final String NEXT_KEY = "70";
    private static final String PAGINATION_KEY = "&paginationKey=";
    private static final String URL_TO_PARSE =
            "/cardTransactions/V01/?contractId=CONTRACTID&cardTransactionType=C"
                    + PAGINATION_KEY
                    + NEXT_KEY
                    + "&pageSize=50";
    private static final String URL_TO_PARSE_NO_KEY =
            "/cardTransactions/V01/?contractId=CONTRACTID&cardTransactionType=C" + "&pageSize=50";

    @Test
    public void splitUtlGetKey_properNextKey() {
        Option<String> nextKeyString = BbvaUtils.splitGetPaginationKey(URL_TO_PARSE);
        assertTrue(nextKeyString.isDefined());
        assertEquals(NEXT_KEY, nextKeyString.get());
    }

    @Test
    public void shouldPassUsernameIfItIsValidDNI() {
        // when
        String result = BbvaUtils.formatUser("91005186M");

        // then
        assertThat(result).isEqualTo("0019-091005186M");
    }

    @Test
    public void shouldPassUsernameIfItIsValidOtherDNI() {
        // when
        String result = BbvaUtils.formatUser("33X");

        // then
        assertThat(result).isEqualTo("0019-000000033X");
    }

    @Test
    public void shouldPassUsernameIfItIsValidOtherInvalidDNI() {
        // when
        String result = BbvaUtils.formatUser("32X");

        // then
        assertThat(result).isNull();
    }

    @Test
    public void shouldNotPassUsernameIfItIsInvalidDNI() {
        // when
        String result = BbvaUtils.formatUser("91005187M");

        // then
        assertThat(result).isNull();
    }

    @Test
    public void shouldPassUsernameIfItIsValidNIE() {
        // when
        String result = BbvaUtils.formatUser("X5211866C");

        // then
        assertThat(result).isEqualTo("0019-X5211866C");
    }

    @Test
    public void shouldPassUsernameIfItIsValidShortNIE() {
        // when
        String result = BbvaUtils.formatUser("X12345V");

        // then
        assertThat(result).isEqualTo("0019-X12345V");
    }

    @Test
    public void shouldPassUsernameIfItIsForeignPassport() {
        // when
        String result = BbvaUtils.formatUser("YY401115D");

        // then
        assertThat(result).isEqualTo("0019-YY401115D");
    }

    @Test
    public void shouldNotPassUsernameIfItIsInvalidNIE() {
        // when
        String result = BbvaUtils.formatUser("Y0401116D");

        // then
        assertThat(result).isNull();
    }

    @Test
    public void shouldPassUsernameIfItIsValidPassport() {
        // when
        String result = BbvaUtils.formatUser("C029532");

        // then
        assertThat(result).isEqualTo("0019-C029532");
    }

    @Test
    public void shouldPassUsernameIfItIs9LenghtNumber() {
        // when
        String result = BbvaUtils.formatUser("029532123");

        // then
        assertThat(result).isEqualTo("4041340295321239");
    }

    @Test
    public void shouldPassUsernameIfItIs7LenghtNumber() {
        // when
        String result = BbvaUtils.formatUser("0123456");

        // then
        assertThat(result).isEqualTo("0123456");
    }

    @Test
    public void shouldPassUsernameIfItIsNot9or7LenghtNumber() {
        // when
        String result = BbvaUtils.formatUser("01234567");

        // then
        assertThat(result).isEqualTo("0019-01234567");
    }

    @Test
    public void shouldNotPassUsernameIfItIsNotAdmitted() {
        // when
        String result = BbvaUtils.formatUser("4444555566667777999");

        // then
        assertThat(result).isNull();
    }

    @Test
    public void shouldPassUsernameIfItIsAnonymousCard() {
        // when
        String result = BbvaUtils.formatUser("4444555566667777");

        // then
        assertThat(result).isEqualTo("4444555566667777");
    }

    @Test
    public void shouldNotPassUsernameIfItIsNull() {
        // when
        String result = BbvaUtils.formatUser(null);

        // then
        assertThat(result).isNull();
    }
}
