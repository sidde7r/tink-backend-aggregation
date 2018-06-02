package se.tink.backend.product.execution.unit.agents.seb.mortgage;

import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusResponse;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusSignRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusSignResponse;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetRateRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetRateResponse;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.LoanPostRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.LoanPostResponse;
import se.tink.backend.product.execution.model.ProductType;
import se.tink.backend.product.execution.tracker.CreateProductExecutorTracker;

public class SEBMortgageApiClientImpl implements SEBMortgageApiClient {
    private static final int HTTP_OK_STATUS_CODE = ClientResponse.Status.OK.getStatusCode();
    private final HttpClient client;
    private final CreateProductExecutorTracker tracker;

    @Inject
    public SEBMortgageApiClientImpl(HttpClient client, CreateProductExecutorTracker tracker) {
        this.client = client;
        this.tracker = tracker;
    }

    @Override
    public LoanPostResponse createMortgageCase(LoanPostRequest loan) {
        try {
            LoanPostResponse loanPostResponse = client.post(loan, LoanPostResponse.class);
            tracker.trackSubmitApplication("seb", ProductType.MORTGAGE, HTTP_OK_STATUS_CODE);
            return loanPostResponse;
        } catch (UniformInterfaceException e) {
            tracker.trackSubmitApplication("seb", ProductType.MORTGAGE, e.getResponse().getStatus());
            throw e;
        }
    }

    @Override
    public GetLoanStatusResponse getMortgageStatus(GetLoanStatusRequest loanStatusRequest) {
        try {
            GetLoanStatusResponse getLoanStatusResponse = client.get(loanStatusRequest, GetLoanStatusResponse.class);
            tracker.trackFetchApplicationStatus("seb", ProductType.MORTGAGE, HTTP_OK_STATUS_CODE);
            return getLoanStatusResponse;
        } catch (UniformInterfaceException e) {
            tracker.trackFetchApplicationStatus("seb", ProductType.MORTGAGE, e.getResponse().getStatus());
            throw e;
        }
    }

    @Override
    public GetLoanStatusSignResponse getMortgageStatusSign(GetLoanStatusSignRequest mortgageStatusSignRequest) {
        try {
            GetLoanStatusSignResponse getLoanStatusSignResponse = client
                    .get(mortgageStatusSignRequest, GetLoanStatusSignResponse.class);
            tracker.trackFetchApplicationSignStatus("seb", ProductType.MORTGAGE, HTTP_OK_STATUS_CODE);
            return getLoanStatusSignResponse;
        } catch (UniformInterfaceException e) {
            tracker.trackFetchApplicationSignStatus("seb", ProductType.MORTGAGE, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == ClientResponse.Status.FORBIDDEN.getStatusCode()) {
                // For what we know now 403 errors from SEB are always caused by TryggVe (SEB's own identity validation system)
                GetLoanStatusSignResponse response = new GetLoanStatusSignResponse();
                response.setStatus(GetLoanStatusSignResponse.BankIdStatus.USER_VALIDATION_ERROR);
                return response;
            }

            throw e;
        }
    }

    @Override
    public GetRateResponse getRate(GetRateRequest rateRequest) {
        try {
            GetRateResponse getRateResponse = client.get(rateRequest, GetRateResponse.class);
            tracker.trackFetchProductInformation("seb", ProductType.MORTGAGE, HTTP_OK_STATUS_CODE);
            return getRateResponse;
        } catch (UniformInterfaceException e) {
            tracker.trackFetchProductInformation("seb", ProductType.MORTGAGE, e.getResponse().getStatus());
            throw e;
        }
    }
}
