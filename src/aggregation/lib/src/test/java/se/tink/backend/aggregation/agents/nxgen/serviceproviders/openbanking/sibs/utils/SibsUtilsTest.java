package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.Consent;

public class SibsUtilsTest {

    private static final String EXPECTED_SIGNING_STRING =
            "digest: SHA-256=dummyDigest\n"
                    + "tpp-transaction-id: dummyTransactionId\n"
                    + "tpp-request-id: dummyRequestId\n"
                    + "date: dummyDate";

    private static final String EXPECTED_DIGEST = "LWFzxWAWcmfw7V0AsX2Tx4pNg3s0WCVJ7w5bO4zwJhs=";

    private static final DateTimeFormatter PAGINATION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(SibsConstants.Formats.PAGINATION_DATE_FORMAT);

    @Test
    public void shouldReturnSigningString() {
        String signature =
                SibsUtils.getSigningString(
                        "dummyDigest", "dummyTransactionId", "dummyRequestId", "dummyDate");

        Assertions.assertThat(signature).isEqualTo(EXPECTED_SIGNING_STRING);
    }

    @Test
    public void shouldCalculateDigest() {
        String signature = SibsUtils.getDigest("dummyBody");

        Assertions.assertThat(signature).isEqualTo(EXPECTED_DIGEST);
    }

    @Test
    public void shouldReturnTransactionsFromBeginningPaginationDate() {
        Consent consent = new Consent("dummyId", LocalDateTime.now().toString());

        String paginationDate = SibsUtils.getPaginationDate(consent);

        Assertions.assertThat(paginationDate).isEqualTo("1970-01-01");
    }

    @Test
    public void shouldReturnLast90DaysTransactionsPaginationDate() {
        Consent consent = new Consent("dummyId", LocalDateTime.now().minusMinutes(30L).toString());

        String paginationDate = SibsUtils.getPaginationDate(consent);

        String expectedPaginationDate =
                PAGINATION_DATE_FORMATTER.format(LocalDateTime.now().minusDays(89));

        Assertions.assertThat(paginationDate).isEqualTo(expectedPaginationDate);
    }
}
