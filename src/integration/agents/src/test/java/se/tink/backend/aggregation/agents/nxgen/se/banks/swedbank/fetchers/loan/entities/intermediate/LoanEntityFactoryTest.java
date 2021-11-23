package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.intermediate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.DetailedLoanResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LoanEntityFactoryTest {

    private LoanEntityFactory loanEntityFactory;

    @Before
    public void setUp() {
        loanEntityFactory = new LoanEntityFactory();
    }

    @Test
    public void shouldCreateCarLoanEntityIfLoanDetailIsCarLoan() {
        BaseAbstractLoanEntity result =
                loanEntityFactory.create(CarLoanEntity.class, getLoanOverview(), getLoanDetails());

        Assert.assertTrue(result instanceof CarLoanEntity);
    }

    @Test
    public void shouldCreateCollateralsLoanEntityIfLoanDetailIsCarLoan() {
        BaseAbstractLoanEntity result =
                loanEntityFactory.create(
                        CollateralsLoanEntity.class, getLoanOverview(), getLoanDetails());

        Assert.assertTrue(result instanceof CollateralsLoanEntity);
    }

    @Test
    public void shouldCreateConsumptionLoanEntityIfLoanDetailIsCarLoan() {
        BaseAbstractLoanEntity result =
                loanEntityFactory.create(
                        ConsumptionLoanEntity.class, getLoanOverview(), getLoanDetails());

        Assert.assertTrue(result instanceof ConsumptionLoanEntity);
    }

    private LoanEntity getLoanOverview() {
        return SerializationUtils.deserializeFromString(
                "{ "
                        + "\"name\": \"name\","
                        + "\"id\": \"id\","
                        + "\"account\": {},"
                        + "\"interestRate\": 1.0,"
                        + "\"debt\": {}, "
                        + "\"type\": \"type\""
                        + "}",
                LoanEntity.class);
    }

    private DetailedLoanResponse getLoanDetails() {
        return SerializationUtils.deserializeFromString(
                "{ "
                        + "\"loanLender\": \"loanLender\","
                        + "\"links\": {},"
                        + "\"loanDetail\": {"
                        + " \"termsAndConditions\": {"
                        + "      \"changeType\": \"FIXING\","
                        + "      \"renewalTakesEffectDate\": \"1992-04-30\"}},"
                        + "\"upcomingInvoice\": {},"
                        + "\"debt\": {},"
                        + "\"loan\": { \"account\": {}},"
                        + "\"interestSpecifications\": []"
                        + "}",
                DetailedLoanResponse.class);
    }
}
