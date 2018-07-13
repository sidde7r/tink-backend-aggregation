package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.CreateProductExecutor;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.mapping.ApplicationToLoanPostRequestMapper;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.MortgageStatus;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.application.InvalidApplicationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.log.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.CreateProductResponse;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.aggregation.rpc.ProductType;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.application.ApplicationState;
import se.tink.backend.core.application.RefreshApplicationParameterKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class SEBCreateProductExecutor implements CreateProductExecutor {
    private final AgentContext context;
    private final Credentials credentials;
    private final AggregationLogger log;
    private final SEBMortgageApiClient mortgageApiClient;
    private final SEBMortgageBankIdCollector mortgageBankIdCollector;
    private final ApplicationToLoanPostRequestMapper mortgageApplicationMapper;
    private final ProductInformationGetRatesMapper ratesRequestMapper;

    @Inject
    public SEBCreateProductExecutor(
            AgentContext context,
            Credentials credentials,
            AggregationLogger log,
            SEBMortgageApiClient mortgageApiClient,
            SEBMortgageBankIdCollector mortgageBankIdCollector,
            ApplicationToLoanPostRequestMapper mortgageApplicationMapper,
            ProductInformationGetRatesMapper ratesRequestMapper) {
        this.context = context;
        this.credentials = credentials;
        this.log = log;
        this.mortgageApiClient = mortgageApiClient;
        this.mortgageBankIdCollector = mortgageBankIdCollector;
        this.mortgageApplicationMapper = mortgageApplicationMapper;
        this.ratesRequestMapper = ratesRequestMapper;
    }

    // TODO: Error handling depending on how SEB api works with errors (if they send statuses != 200 in those cases)
    @Override
    public CreateProductResponse create(GenericApplication application) throws BankIdException, InvalidApplicationException {
        LoanPostResponse mortgageCase;

        try {
            // Post application to SEB (and initiate BankID sign)
            LoanPostRequest loanPostRequest = mortgageApplicationMapper.toLoanRequest(application);

            mortgageCase = mortgageApiClient.createMortgageCase(loanPostRequest);
        } catch (UniformInterfaceException e) {
            if (Objects.equals(e.getResponse().getStatus(), Response.Status.BAD_REQUEST.getStatusCode())) {
                log.error("Invalid application: " + e.getResponse().getEntity(String.class));
                throw new InvalidApplicationException();
            }

            throw e;
        }

        String externalId = signApplication(mortgageCase);
        
        return new CreateProductResponse(externalId);
    }

    private String signApplication(LoanPostResponse mortgageCase) throws BankIdException {
        // The external application id
        String externalApplicationId = mortgageCase.getApplicationId();

        // Wait for BankID sign to complete
        openBankIdApp();

        GetLoanStatusSignResponse.BankIdStatus finalBankIdStatus = mortgageBankIdCollector.collect(externalApplicationId);

        // If an error occured, throw exception
        switch (finalBankIdStatus) {
        case COMPLETE:
            return mortgageCase.getApplicationId();
        case USER_CANCEL:
            throw BankIdError.CANCELLED.exception();
        case USER_VALIDATION_ERROR:
            throw BankIdError.USER_VALIDATION_ERROR.exception();
        default:
            log.error(String.format("Failed to sign mortgage: {\"statusCode\": %s}", finalBankIdStatus));
            throw new IllegalStateException(String.format("[BankId status]: %s", finalBankIdStatus));
        }
    }

    private void openBankIdApp() {
        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);
        context.requestSupplementalInformation(credentials, false);
    }

    @Override
    public void fetchProductInformation(ProductType type, UUID productInstanceId,
            Map<FetchProductInformationParameterKey, Object> parameters) {
        switch (type) {
        case MORTGAGE:
            fetchMortgageRate(productInstanceId, parameters);
            break;
        default:
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    private void fetchMortgageRate(UUID productInstanceId,
            Map<FetchProductInformationParameterKey, Object> parameters) {
        GetRateRequest rateRequest = ratesRequestMapper.toRateRequest(parameters);
        GetRateResponse rateResponse = mortgageApiClient.getRate(rateRequest);
        HashMap<ProductPropertyKey, Object> productProperties = ratesRequestMapper.toProductProperties(rateResponse);
        context.updateProductInformation(productInstanceId, productProperties);
    }

    @Override
    public void refreshApplication(ProductType type, UUID applicationId,
            Map<RefreshApplicationParameterKey, Object> parameters) {
        GetLoanStatusRequest getLoanStatusRequest = createLoanStatusRequest(parameters);

        ApplicationState applicationState = null;

        try {
            GetLoanStatusResponse mortgageStatus = mortgageApiClient.getMortgageStatus(getLoanStatusRequest);
            applicationState = createApplicationState(mortgageStatus);
            log.debug(
                    String.format("Refresh application [applicationId:%s]. Parameters: %s. External status: %s.",
                            UUIDUtils.toTinkUUID(applicationId), SerializationUtils.serializeToString(parameters), mortgageStatus.getStatus().toString()));
        } catch (UniformInterfaceException e) {
           if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
               applicationState = new ApplicationState();
               applicationState.setNewApplicationStatus(ApplicationStatusKey.EXPIRED);
               log.debug(
                       String.format("Refresh application [applicationId:%s]. Parameters: %s. Bank has deleted application.",
                               UUIDUtils.toTinkUUID(applicationId), SerializationUtils.serializeToString(parameters)));
           } else {
               throw e;
           }
        }

        context.updateApplication(applicationId, applicationState);
    }

    private ApplicationState createApplicationState(GetLoanStatusResponse mortgageStatus) {
        ApplicationState applicationState = new ApplicationState();

        Optional<ApplicationStatusKey> statusKey = getApplicationStatus(mortgageStatus.getStatus());
        if (statusKey.isPresent()) {
            applicationState.setNewApplicationStatus(statusKey.get());
        }
        
        applicationState.setApplicationProperty(
                ApplicationPropertyKey.EXTERNAL_STATUS, mortgageStatus.getStatus());
        applicationState.setApplicationProperty(
                ApplicationPropertyKey.EXTERNAL_STATUS_DESCRIPTION, mortgageStatus.getDescription());

        return applicationState;
    }
    
    private Optional<ApplicationStatusKey> getApplicationStatus(MortgageStatus mortgageStatus) {
        if (mortgageStatus == null) {
            return Optional.empty();
        }
        
        switch(mortgageStatus) {
        case CUSTOMER_DECLINED:
            return Optional.of(ApplicationStatusKey.ABORTED);
        case MORTGAGE_TRANSFERRED:
            return Optional.of(ApplicationStatusKey.EXECUTED);
        case APPLICATION_REJECTED:
            return Optional.of(ApplicationStatusKey.REJECTED);
        case APPLICATION_APPROVED:
            return Optional.of(ApplicationStatusKey.APPROVED);
        case INCOMPLETE_APPLICATION:
        case SEB_WILL_CONTACT_CUSTOMER:
            return Optional.of(ApplicationStatusKey.SUPPLEMENTAL_INFORMATION_REQUIRED);
        case APPLICATION_CREATED:
            return Optional.of(ApplicationStatusKey.SIGNED);
        default:
            log.warn(String.format("The mortgage status '%s' is not mapped to an application status.", mortgageStatus));
            return Optional.empty();
        }
    }

    private GetLoanStatusRequest createLoanStatusRequest(Map<RefreshApplicationParameterKey, Object> parameters) {
        Preconditions.checkArgument(parameters.containsKey(RefreshApplicationParameterKey.EXTERNAL_ID));

        String externalApplicationId = (String) parameters.get(RefreshApplicationParameterKey.EXTERNAL_ID);
        return new GetLoanStatusRequest(externalApplicationId);
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        mortgageApiClient.attachHttpFilters(filterFactory);
    }
}
