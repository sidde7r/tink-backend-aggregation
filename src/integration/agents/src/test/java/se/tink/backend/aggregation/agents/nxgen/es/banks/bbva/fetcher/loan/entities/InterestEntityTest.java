package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class InterestEntityTest {

    @Test
    public void testLoanInterestReviewDateIsNull() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InterestResponse entities =
                mapper.readValue(InterestEntityTestData.LOAN_INTEREST_DATA, InterestResponse.class);

        Assert.assertEquals(
                null,
                entities.getInterests().stream().findFirst().get().getReviewDateAsLocalDate());
    }
}
