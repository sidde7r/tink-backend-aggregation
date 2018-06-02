package se.tink.backend.product.execution.unit.agents.seb.mortgage;

import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusResponse;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusSignRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusSignResponse;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetRateRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetRateResponse;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.LoanPostRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.LoanPostResponse;

public interface SEBMortgageApiClient {
    LoanPostResponse createMortgageCase(LoanPostRequest loan);
    GetLoanStatusResponse getMortgageStatus(GetLoanStatusRequest getLoanStatusRequest);
    GetLoanStatusSignResponse getMortgageStatusSign(GetLoanStatusSignRequest getLoanStatusSignRequest);
    GetRateResponse getRate(GetRateRequest rateRequest);
}
