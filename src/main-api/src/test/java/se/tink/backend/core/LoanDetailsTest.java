package se.tink.backend.core;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import se.tink.libraries.uuid.UUIDUtils;
import static org.junit.Assert.*;

public class LoanDetailsTest {
    private static final String ACCOUNT_ID = "627fba23f10a4eb9bb667be6f144151f";
    private static final boolean HAS_CO_APPLICANT = true;
    private static final ImmutableList<String> APPLICANTS = ImmutableList.of("Andreas","Fredrik");
    private static final ImmutableList<String> NEW_APPLICANTS = ImmutableList.of("Daniel","Elias");
    private static final String SECURITY = "Wallingatan 5";

    @Test
    public void testEquals() {
        LoanDetails newDetails = createDetails();
        LoanDetails foundDetails = createDetails();

        assertTrue(newDetails.equals(foundDetails));

        newDetails.setApplicants(NEW_APPLICANTS);
        assertFalse(newDetails.equals(foundDetails));
    }

    private LoanDetails createDetails() {

        LoanDetails details = new LoanDetails();
        details.setAccountId(UUIDUtils.fromTinkUUID(ACCOUNT_ID));
        details.setCoApplicant(HAS_CO_APPLICANT);
        details.setApplicants(APPLICANTS);
        details.setLoanSecurity(SECURITY);

        return details;
    }
}