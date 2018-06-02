package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostResponse;
import se.tink.backend.aggregation.log.ClientFilterFactory;

public interface SEBMortgageApiClient {
    LoanPostResponse createMortgageCase(LoanPostRequest loan);
    GetLoanStatusResponse getMortgageStatus(GetLoanStatusRequest getLoanStatusRequest);
    GetLoanStatusSignResponse getMortgageStatusSign(GetLoanStatusSignRequest getLoanStatusSignRequest);
    GetRateResponse getRate(GetRateRequest rateRequest);

    void attachHttpFilters(ClientFilterFactory filterFactory);
}
