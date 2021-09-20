package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.rpc;

import static org.junit.Assert.assertEquals;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc.LoanEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class LoanEntityTest {

    public static final String ANNUTITY_LOAN = "ÅBAL";
    public static final String STUDENT_LOAN = "ÅBSL";
    public static final String STUDENT_AID = "ÅBSM";

    @Test
    @Parameters(method = "getParameters")
    public void itShouldReturnExpectedAccountNameWithGivenLoanType(
            String expectedUniqueIdentifier, String expectedAccountName, String loanType) {
        assertEquals(
                expectedAccountName,
                ReflectionTestUtils.invokeMethod(getDeptSpecification(loanType), "getAccountName"));
    }

    @Test
    @Parameters(method = "getParameters")
    public void itShouldReturnExpectedUniqueIdentifierWithGivenLoanType(
            String expectedUniqueIdentifier, String expectedAccountName, String loanType) {
        assertEquals(
                expectedUniqueIdentifier,
                ReflectionTestUtils.invokeMethod(
                        getDeptSpecification(loanType), "getUniqueIdentifier", "9901011234"));
    }

    private Object[] getParameters() {
        return new Object[] {
            new Object[] {"9901011234annuitetslan", "Annuitetslån", ANNUTITY_LOAN},
            new Object[] {"9901011234studielan", "Studielån", STUDENT_LOAN},
            new Object[] {"9901011234studiemedel", "Studiemedel", STUDENT_AID},
            new Object[] {"", "", ""},
            new Object[] {"null", "null", null}
        };
    }

    private static LoanEntity getDeptSpecification(String loanType) {
        return SerializationUtils.deserializeFromString(
                "{\"skuldspecifikation\":[{\"handelse\":\"Ingående skuld\",\"handelsedatum\":1609455600000,\"summa\":11880},{\"handelse\":\"Expeditionsavgift 2021\",\"summa\":150},{\"handelse\":\"Betald expeditionsavgift 2021\",\"handelsedatum\":1611874800000,\"summa\":-150},{\"handelse\":\"Betalt årsbelopp 2021\",\"summa\":-476},{\"handelse\":\"Utgående skuld\",\"handelsedatum\":1612911600000,\"summa\":11404}],\"lanetyp\":\""
                        + loanType
                        + "\",\"lopnummer\":2,\"senasteBerakningsdatum\":1611874800000,\"skuldbelopp\":11404,\"skuldrattat\":\"\",\"klartext\":false,\"laneTypKlartext\":\"laan.ÅBAL\"}",
                LoanEntity.class);
    }
}
