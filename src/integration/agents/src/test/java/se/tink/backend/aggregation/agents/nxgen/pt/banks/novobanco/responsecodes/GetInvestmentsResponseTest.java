package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.responsecodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail.InvestmentTestData.FAILED_CALL;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail.InvestmentTestData.INVESTMENTS_AVAILABLE;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail.InvestmentTestData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment.GetInvestmentsResponse;

public class GetInvestmentsResponseTest {

    @Test
    public void shouldReportSuccessIfSuccessfulGetInvestmentsResponse() {
        GetInvestmentsResponse response = InvestmentTestData.getResponse(INVESTMENTS_AVAILABLE);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldReportFailureIfUnsuccessfulGetInvestmentsResponse() {
        GetInvestmentsResponse response = InvestmentTestData.getResponse(FAILED_CALL);
        assertFalse(response.isSuccessful());
    }
}
