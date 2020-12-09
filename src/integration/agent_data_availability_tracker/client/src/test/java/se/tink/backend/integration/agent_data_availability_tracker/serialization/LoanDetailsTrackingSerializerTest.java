package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.LoanDetails;

public class LoanDetailsTrackingSerializerTest {

    private static final String SECRET_STRING = "SecretValue";

    @Test
    public void ensureInstrument_withAllNullValues_doesNotThrowException() {
        List<FieldEntry> entries = new LoanDetailsTrackingSerializer(new LoanDetails()).buildList();

        Assert.assertTrue(
                "Failed: all values null",
                entries.stream().map(FieldEntry::getValue).allMatch("null"::equals));
    }

    @Test
    public void ensureSecretAccountFields_areRedacted() {
        ImmutableSet<String> secretFieldKeys =
                ImmutableSet.<String>builder()
                        .add("LoanDetails.loanSecurity")
                        .add("LoanDetails.applicants")
                        .build();

        LoanDetails loanDetails = new LoanDetails();
        loanDetails.setLoanSecurity(SECRET_STRING);
        loanDetails.setApplicants(Arrays.asList(SECRET_STRING, SECRET_STRING));

        List<FieldEntry> entries = new LoanDetailsTrackingSerializer(loanDetails).buildList();

        Assert.assertTrue(
                "Failed: all secret fields are unlisted",
                TrackingSerializationTestHelper.isAllUnlisted(secretFieldKeys, entries));
    }
}
