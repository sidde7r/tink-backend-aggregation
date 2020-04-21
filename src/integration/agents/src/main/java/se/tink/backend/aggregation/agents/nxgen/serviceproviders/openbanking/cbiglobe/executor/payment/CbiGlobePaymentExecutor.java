package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;

public class CbiGlobePaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final CbiGlobeApiClient apiClient;
    private final List<PaymentResponse> paymentResponses = new ArrayList<>();
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PersistentStorage persistentStorage;
    private final StrongAuthenticationState strongAuthenticationState;

    public CbiGlobePaymentExecutor(
            CbiGlobeApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            PersistentStorage persistentStorage,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.persistentStorage = persistentStorage;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        fetchToken();

        persistentStorage.put(QueryKeys.STATE, strongAuthenticationState.getState());

        AccountEntity creditorEntity = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtorEntity = AccountEntity.debtorOf(paymentRequest);
        InstructedAmountEntity instructedAmountEntity = InstructedAmountEntity.of(paymentRequest);
        RemittanceInformationStructuredEntity remittanceInformationStructuredEntity =
                RemittanceInformationStructuredEntity.of(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withDebtorAccount(debtorEntity)
                        .withInstructedAmount(instructedAmountEntity)
                        .withCreditorAccount(creditorEntity)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .withRemittanceInformationUnstructured(
                                paymentRequest.getPayment().getReference().getValue())
                        .withRemittanceInformationStructured(remittanceInformationStructuredEntity)
                        .withTransactionType(FormValues.TRANSACTION_TYPE)
                        .build();

        CreatePaymentResponse payment = apiClient.createPayment(createPaymentRequest);
        persistentStorage.put(
                StorageKeys.LINK,
                payment.getLinks().getUpdatePsuAuthenticationRedirect().getHref());
        return payment.toTinkPaymentResponse(getPaymentType(paymentRequest));
    }

    private boolean fetchToken() {
        try {
            if (!apiClient.isTokenValid()) {
                apiClient.getAndSaveToken();
            }
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            if (message.contains(MessageCodes.NO_ACCESS_TOKEN_IN_STORAGE.name())) {
                apiClient.getAndSaveToken();
            } else {
                throw e;
            }
        }

        return true;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse =
                apiClient
                        .getPayment(paymentRequest.getPayment().getUniqueId())
                        .toTinkPaymentResponse(paymentRequest.getPayment().getType());
        paymentResponses.add(paymentResponse);
        return paymentResponse;
    }

    public CreatePaymentResponse fetchPaymentStatus(String paymentId) {
        CreatePaymentResponse paymentResponse = apiClient.getPaymentStatus(paymentId);
        return paymentResponse;
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentAuthorizationException {

        openThirdPartyApp(new URL(persistentStorage.get(StorageKeys.LINK)));
        waitForSupplementalInformation();

        CreatePaymentResponse createPaymentResponse =
                fetchPaymentStatus(paymentMultiStepRequest.getPayment().getUniqueId());
        String paymentStatus = createPaymentResponse.getTransactionStatus();
        String scaStatus = createPaymentResponse.getScaStatus();
        String psuAuthenticationStatus = createPaymentResponse.getPsuAuthenticationStatus();

        switch (paymentStatus) {
                // After signing PIS
            case "ACCP":
            case "ACSC":
            case "ACSP":
            case "ACTC":
            case "ACWC":
            case "ACWP":
                {
                    if ("AUTHENTICATED".equalsIgnoreCase(psuAuthenticationStatus)
                            && "VERIFIED".equalsIgnoreCase(scaStatus)) {
                        return new PaymentMultiStepResponse(
                                createPaymentResponse.toTinkPaymentResponse(
                                        paymentMultiStepRequest.getPayment().getType()),
                                AuthenticationStepConstants.STEP_FINALIZE,
                                new ArrayList<>());
                    }
                }
                break;
                // Before signing PIS
            case "RCVD":
            case "PDNG":
                {
                    if ("IDENTIFICATION_REQUIRED".equalsIgnoreCase(psuAuthenticationStatus)
                            || "AUTHENTICATION_REQUIRED"
                                    .equalsIgnoreCase(psuAuthenticationStatus)) {
                        persistentStorage.put(
                                StorageKeys.LINK,
                                createPaymentResponse
                                        .getLinks()
                                        .getUpdatePsuAuthenticationRedirect()
                                        .getHref());

                    } else if ("INITIATED".equalsIgnoreCase(scaStatus)) {
                        persistentStorage.put(
                                StorageKeys.LINK,
                                createPaymentResponse.getLinks().getScaRedirect().getHref());
                    }
                    return new PaymentMultiStepResponse(
                            createPaymentResponse.toTinkPaymentResponse(
                                    paymentMultiStepRequest.getPayment().getUniqueId(),
                                    paymentMultiStepRequest.getPayment().getType()),
                            "IN_PROGRESS",
                            new ArrayList<>());
                }
            case "RJCT": // failed case
                {
                    if ("AUTHENTICATION_FAILED".equalsIgnoreCase(psuAuthenticationStatus)) {
                        throw new PaymentAuthorizationException();
                    }
                }
        }

        return null; // this method should never return null- If this scenario happen then
                     // CBI Globe might have changed Payment Status Codes
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        // Not implemented on banks side
        return new PaymentListResponse(paymentResponses);
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    private void waitForSupplementalInformation() {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(), 9L, TimeUnit.MINUTES);
    }

    protected PaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<AccountIdentifier.Type, AccountIdentifier.Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentTypeMapper
                .translate(accountIdentifiersKey)
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        "No PaymentType found for your AccountIdentifiers pair "
                                                + accountIdentifiersKey));
    }

    private static final GenericTypeMapper<PaymentType, Pair<Type, Type>>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                    genericBuilder()
                            .put(PaymentType.SEPA, new Pair<>(Type.IBAN, Type.IBAN))
                            .build();
}
