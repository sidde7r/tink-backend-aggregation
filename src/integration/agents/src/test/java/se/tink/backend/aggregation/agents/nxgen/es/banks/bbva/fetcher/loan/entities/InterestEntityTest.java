package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities;

import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class InterestEntityTest {

    private static String loanInterestData =
            "{\n"
                    + "   \"interests\":[\n"
                    + "      {\n"
                    + "         \"percentage\":1.657\n"
                    + "      }\n"
                    + "   ]\n"
                    + "}";

    @Test(expected = NullPointerException.class)
    public void testLoanInterestReviewDateIsNull() throws IOException {
        InterestResponse entities =
                SerializationUtils.deserializeFromString(loanInterestData, InterestResponse.class);

        Assert.assertEquals(
                null,
                entities.getInterests().stream().findFirst().get().getReviewDateAsLocalDate());
    }

    @Ignore
    private class InterestResponse {
        private List<InterestEntity> interests;

        public List<InterestEntity> getInterests() {
            return interests;
        }
    }
}
