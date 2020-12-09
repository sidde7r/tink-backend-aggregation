package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import com.google.common.collect.ImmutableSet;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.integration.agent_data_availability_tracker.common.FieldEntry;
import se.tink.backend.integration.agent_data_availability_tracker.common.TrackingList;

public class LoanTrackingSerializerTest {

    private static final String VALUE_NOT_LISTED = TrackingList.Builder.VALUE_NOT_LISTED;
    private static final String SECRET_STRING = "SecretValue";
    private static final Double SECRET_DOUBLE = Double.valueOf(10.d);
    private static final Integer SECRET_INTEGER = Integer.valueOf(1);
    private static final Boolean SECRET_BOOLEAN = false;
    private static final Date SECRET_DATE = new Date();
    private ImmutableSet<String> secretFieldKeys =
            ImmutableSet.<String>builder()
                    .add("Loan<null>.initialBalance")
                    .add("Loan<null>.numberOfMonthsBound")
                    .add("Loan<null>.interest")
                    .add("Loan<null>.balance")
                    .add("Loan<null>.amortized")
                    .add("Loan<null>.monthlyAmortization")
                    .add("Loan<null>.name")
                    .add("Loan<null>.providerName")
                    .add("Loan<null>.loanNumber")
                    .add("Loan<null>.nextDayOfTermChange")
                    .add("Loan<null>.userModifiedType")
                    .add("Loan<null>.updated")
                    .build();

    @Test
    public void ensureInstrument_withAllNullValues_doesNotThrowException() {
        List<FieldEntry> entries = new LoanTrackingSerializer(new Loan()).buildList();

        Assert.assertTrue(
                "Failed: all values null",
                entries.stream().map(FieldEntry::getValue).allMatch("null"::equals));
    }

    @Test
    public void ensureSecretAccountFields_areRedacted() {

        Loan loan = new Loan();
        loan.setNumMonthsBound(SECRET_INTEGER);
        loan.setAmortized(SECRET_DOUBLE);
        loan.setBalance(SECRET_DOUBLE);
        loan.setInitialBalance(SECRET_DOUBLE);
        loan.setInitialDate(SECRET_DATE);
        loan.setInterest(SECRET_DOUBLE);
        loan.setLoanNumber(SECRET_STRING);
        loan.setMonthlyAmortization(SECRET_DOUBLE);
        loan.setName(SECRET_STRING);
        loan.setNextDayOfTermsChange(SECRET_DATE);
        loan.setProviderName(SECRET_STRING);
        loan.setUpdated(SECRET_DATE);
        loan.setUserModifiedType(SECRET_BOOLEAN);
        // This one should not be tracked at all
        loan.setSerializedLoanResponse(SECRET_STRING);

        List<FieldEntry> entries = new LoanTrackingSerializer(loan).buildList();

        Assert.assertTrue(
                "Failed: all secret fields are unlusted",
                TrackingSerializationTestHelper.isAllUnlisted(secretFieldKeys, entries));
    }

    @Test
    public void ensureLoanType_isTracked_andIncludedInKey() {

        Loan loan = new Loan();
        loan.setType(Loan.Type.VEHICLE);

        List<FieldEntry> entries = new LoanTrackingSerializer(loan).buildList();

        Assert.assertTrue(
                "Failed: has entry 'Loan<VEHICLE>.type' with value == VEHICLE",
                TrackingSerializationTestHelper.hasFieldWithValue(
                        "Loan<VEHICLE>.type", Loan.Type.VEHICLE.toString(), entries));
    }
}
