package se.tink.backend.product.execution.unit.agents.sbab.mortgage;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.NotImplementedException;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.application.ApplicationState;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.product.execution.unit.agents.CreateProductExecutor;
import se.tink.backend.product.execution.unit.agents.exceptions.application.UnsupportedApplicationException;
import se.tink.backend.product.execution.unit.agents.exceptions.errors.BankIdError;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.BankIdStatus;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.SignFormRequestBody;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request.MortgageApplicationRequest;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request.MortgageSignatureRequest;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.BankIdStartResponse;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.DiscountResponse;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.InterestRateEntity;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.InterestsResponse;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.MortgageSignatureStatus;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.MortgageStatus;
import se.tink.backend.product.execution.configuration.ProductExecutorConfiguration;
import se.tink.backend.product.execution.log.ProductExecutionLogger;
import se.tink.backend.product.execution.model.CreateProductResponse;
import se.tink.backend.product.execution.model.CredentialsUpdate;
import se.tink.backend.product.execution.model.FetchProductInformationParameterKey;
import se.tink.backend.product.execution.model.ProductType;
import se.tink.backend.product.execution.model.RefreshApplicationParameterKey;
import se.tink.backend.product.execution.tracker.CreateProductExecutorTracker;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.Doubles;
import se.tink.libraries.application.GenericApplication;

public class SBABCreateProductExecutor implements CreateProductExecutor {
    private static final ProductExecutionLogger log = new ProductExecutionLogger(SBABCreateProductExecutor.class);

    private static final int BANKID_MAX_ATTEMPTS = 100;

    private final Client client;
    private final MortgageApiClient mortgageApiClient;
    private final MortgageSignClient mortgageSignClient;
    private final SystemServiceFactory systemServiceFactory;

    @Inject
    public SBABCreateProductExecutor(ProductExecutorConfiguration configuration,
            CreateProductExecutorTracker tracker,
            SystemServiceFactory systemServiceFactory) {
        client = new JerseyClientFactory().createClientWithRedirectHandler();

        // TODO: We don't support isSwitchMortgageProviderTest here, as we don't want to introduce dependency to aggregation
        // and we should look for a better solution.
        this.mortgageSignClient = new MortgageSignClient(client, configuration, tracker);
        this.mortgageApiClient = new MortgageApiClient(client, configuration, tracker);

        this.systemServiceFactory = systemServiceFactory;
    }

    @Override
    public CreateProductResponse create(GenericApplication application, CredentialsUpdate credentialsUpdate)
            throws Exception {
        switch (application.getType()) {
        case SWITCH_MORTGAGE_PROVIDER:
            return new CreateProductResponse(switchMortgageProvider(application, credentialsUpdate));
        case OPEN_SAVINGS_ACCOUNT:
            throw new NotImplementedException("Open saving account is not implemented in product executor yet");
        default:
            throw new UnsupportedApplicationException(application.getType());
        }
    }

    @Override
    public void fetchProductInformation(ProductType type, UUID productInstanceId, UUID userId,
            Map<FetchProductInformationParameterKey, Object> parameters) {

        if (!Objects.equal(ProductType.MORTGAGE, type)) {
            log.warn(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(userId)
                    .withProductInstanceId(productInstanceId)
                    .withMessage(String.format("Product information can't be fetched for product type '%s'.", type))
            );
            return;
        }

        if (!parameters.containsKey(FetchProductInformationParameterKey.MARKET_VALUE)) {

            log.error(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(userId)
                    .withProductInstanceId(productInstanceId)
                    .withMessage("Market value is missing.")
            );
            return;
        }

        if (!parameters.containsKey(FetchProductInformationParameterKey.MORTGAGE_AMOUNT)) {
            log.error(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(userId)
                    .withProductInstanceId(productInstanceId)
                    .withMessage("Mortgage amount is missing.")
            );
            return;
        }

        if (!parameters.containsKey(FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS)) {
            log.error(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(userId)
                    .withProductInstanceId(productInstanceId)
                    .withMessage("Number of applicants is missing.")
            );
            return;
        }

        int marketValue = (int) parameters.get(FetchProductInformationParameterKey.MARKET_VALUE);
        int mortgageAmount = (int) parameters.get(FetchProductInformationParameterKey.MORTGAGE_AMOUNT);
        int numberOfApplicants = (int) parameters.get(FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS);

        try {
            InterestsResponse interestResponse = mortgageApiClient.getInterestRates(marketValue, mortgageAmount);

            // Find the 3 months duration rate entity.
            InterestRateEntity rate = Iterables.find(interestResponse.getInterestRates(),
                    rateEntity -> Doubles.fuzzyEquals(rateEntity.getContractDurationInMonths(), 3d, 0.1));

            DiscountResponse discountResponse = mortgageApiClient
                    .getMortgageDiscounts(numberOfApplicants, mortgageAmount,
                            "BYT_BANK");

            HashMap<ProductPropertyKey, Object> properties = new HashMap<>();
            properties.put(ProductPropertyKey.INTEREST_RATE, rate.getCustomerRate() / 100);
            properties.put(ProductPropertyKey.LIST_INTEREST_RATE, rate.getListRate() / 100);
            properties.put(ProductPropertyKey.INTEREST_RATE_DISCOUNT, discountResponse.getDiscount() / 100);
            properties.put(ProductPropertyKey.INTEREST_RATE_DISCOUNT_DESCRIPTION, discountResponse.getDescription());
            properties.put(ProductPropertyKey.INTEREST_RATE_DISCOUNT_DURATION_MONTHS,
                    discountResponse.getNumberOfMonths());



            log.debug(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(userId)
                    .withProductInstanceId(productInstanceId)
                    .withMessage(String.format(
                            "[marketValue:%d, mortgageAmount:%d, numberOfApplicants:%d] %s.",
                             marketValue, mortgageAmount, numberOfApplicants, properties))
            );


            updateProductInformation(systemServiceFactory, userId, productInstanceId, properties);
        } catch (NoSuchElementException e) {
            log.error(ProductExecutionLogger
                            .newBuilder()
                            .withUserId(userId)
                            .withProductInstanceId(productInstanceId)
                            .withMessage(
                                    String.format(
                                            "No interest rate with 3 months duration available  marketValue:%d, mortgageAmount:%d, numberOfApplicants:%d].",
                                             marketValue, mortgageAmount, numberOfApplicants)));
        } catch (Exception e) {
            log.error(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(userId)
                    .withProductInstanceId(productInstanceId)
                    .withMessage("Unable to fetch product information.")
                    .withThrowable(e));
        }
    }

    @Override
    public void refreshApplication(ProductType type, UUID applicationId, UUID userId,
            Map<RefreshApplicationParameterKey, Object> parameters) throws Exception {
        String externalId = getExternalId(parameters);

        Preconditions.checkState(!Strings.isNullOrEmpty(externalId),
                "No external application reference was supplied.");

        ApplicationState applicationState = null;

        try {
            MortgageStatus mortgageStatus = mortgageApiClient.getMortgageStatus(externalId);
            applicationState = createApplicationState(mortgageStatus);
        } catch (UniformInterfaceException e) {
            if (Objects.equal(e.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode())) {
                applicationState = new ApplicationState();
                applicationState.setNewApplicationStatus(ApplicationStatusKey.EXPIRED);
            } else {
                throw e;
            }
        }

        updateApplication(systemServiceFactory, userId, applicationId, applicationState);
    }

    @Override
    public String getProviderName() {
        return "sbab-bankid";
    }

    private ApplicationState createApplicationState(MortgageStatus mortgageStatus) {
        Preconditions.checkArgument(mortgageStatus != null,
                "Mortgage status not available.");

        ApplicationState applicationState = new ApplicationState();

        ApplicationStatusKey statusKey = getApplicationStatus(mortgageStatus);
        applicationState.setNewApplicationStatus(statusKey);
        applicationState.setApplicationProperty(ApplicationPropertyKey.EXTERNAL_STATUS, mortgageStatus.name());

        return applicationState;
    }

    private ApplicationStatusKey getApplicationStatus(MortgageStatus mortgageStatus) {
        switch (mortgageStatus) {
        case MAKULERAD:
            return ApplicationStatusKey.ABORTED;
        case UTBETALT:
            return ApplicationStatusKey.EXECUTED;
        case AVSLAGEN:
            return ApplicationStatusKey.REJECTED;
        case TEKNISKT_FEL:
            return ApplicationStatusKey.ERROR;
        case AVSLAGEN_UC:
        case BEARBETNING_PAGAR:
        case ANSOKAN_REGISTRERAD:
            return ApplicationStatusKey.SIGNED;
        case KOMPLETTERING_KRAVS:
            return ApplicationStatusKey.SUPPLEMENTAL_INFORMATION_REQUIRED;
        case LANEHANDLINGAR_KLARA:
        case LANEHANDLINGAR_INKOMNA:
            return ApplicationStatusKey.APPROVED;
        default:
            throw new IllegalStateException(String.format(
                    "The mortgage status '%s' is not mapped to an application status.", mortgageStatus.name()));
        }
    }

    private String getExternalId(Map<RefreshApplicationParameterKey, Object> parameters) {
        Object parameter = parameters.get(RefreshApplicationParameterKey.EXTERNAL_ID);
        return parameter != null ? String.valueOf(parameter) : null;
    }

    private String switchMortgageProvider(GenericApplication application, CredentialsUpdate credentialsUpdate)
            throws Exception {
        // Create the request objects before calling SBAB api's so that we bail early if models contains errors
        MortgageSignatureRequest signatureRequest = mortgageApiClient.getSignatureRequest(application);
        MortgageApplicationRequest mortgageApplicationRequest = mortgageApiClient
                .getMortgageApplicationRequest(application);

        mortgageApiClient.setRemoteIp(application.getRemoteIp());

        String signatureId = mortgageApiClient.createSignature(signatureRequest);
        signMortgageSignature(signatureId, credentialsUpdate);

        return mortgageApiClient.sendApplication(mortgageApplicationRequest, signatureId);
    }

    private void signMortgageSignature(String signatureId, CredentialsUpdate credentialsUpdate)
            throws Exception {
        SignFormRequestBody signFormRequestBody = mortgageSignClient.initiateSignProcess(signatureId);

        BankIdStatus bankIdStatus = signMortgageWithMobileBankId(signFormRequestBody, credentialsUpdate);

        switch (bankIdStatus) {
        case DONE:
            MortgageSignatureStatus finalStatus = mortgageApiClient.getMortgageSigningStatus(signatureId);

            if (Objects.equal(finalStatus, MortgageSignatureStatus.SUCCESSFUL)) {

                log.info(ProductExecutionLogger
                        .newBuilder()
                        .withMessage("Successfully created and signed a new mortgage application signature."));
                return;
            } else {
                throw new IllegalStateException(
                        String.format("[BankIdStatus: %s, MortgageSignatureStatus: %s]", bankIdStatus, finalStatus));
            }
        case CANCELLED:
            throw BankIdError.CANCELLED.exception();
        case TIMEOUT:
            throw BankIdError.TIMEOUT.exception();
        case FAILED_UNKNOWN:
        default:
            throw new IllegalStateException(String.format("[BankIdStatus: %s]", bankIdStatus));
        }
    }

    private BankIdStatus signMortgageWithMobileBankId(SignFormRequestBody signFormRequestBody,
            CredentialsUpdate credentialsUpdate) throws Exception {
        BankIdStartResponse startResponse = mortgageSignClient.initiateSign(signFormRequestBody);

        updateCredentialStatus(systemServiceFactory, credentialsUpdate);

        for (int i = 0; i < BANKID_MAX_ATTEMPTS; i++) {
            BankIdStatus bankIdStatus = mortgageSignClient.getStatus(signFormRequestBody, startResponse.getOrderRef());

            if (!Objects.equal(bankIdStatus, BankIdStatus.WAITING)) {
                return bankIdStatus;
            }

            Thread.sleep(2000);
        }

        return BankIdStatus.TIMEOUT;
    }
}
