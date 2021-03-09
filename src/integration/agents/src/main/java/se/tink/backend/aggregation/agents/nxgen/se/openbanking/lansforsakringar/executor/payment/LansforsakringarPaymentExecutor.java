package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.SCAValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarDateUtil;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.SignBasketResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountIbanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountNumbersResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.GirosCreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CreateBasketResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CrossBorderPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CrossBorderPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticGirosPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
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
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class LansforsakringarPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static CountryDateHelper dateHelper = new CountryDateHelper(DEFAULT_LOCALE);

    private LansforsakringarApiClient apiClient;
    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;

    private static final DisplayAccountIdentifierFormatter GIRO_FORMATTER =
            new DisplayAccountIdentifierFormatter();

    public LansforsakringarPaymentExecutor(
            LansforsakringarApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {

        final Payment payment = paymentRequest.getPayment();
        final AmountEntity amount = new AmountEntity(payment.getExactCurrencyAmount());
        final AccountEntity debtor =
                new AccountEntity(payment.getDebtor().getAccountNumber(), amount.getCurrency());

        String executionDate =
                Optional.ofNullable(payment.getExecutionDate())
                        .map(
                                providedDate ->
                                        LansforsakringarDateUtil.getCurrentOrNextBusinessDate(
                                                        providedDate)
                                                .format(DateTimeFormatter.ISO_DATE))
                        .orElse(null);

        Type accountIdentifierType =
                paymentRequest.getPayment().getCreditor().getAccountIdentifierType();

        validateDebtorAccount(debtor, accountIdentifierType);

        switch (accountIdentifierType) {
            case SE_BG:
            case SE_PG:
                return createDomesticGirosPayment(payment, debtor, amount, executionDate);
            case SE:
                return createDomesticPayment(payment, debtor, amount, executionDate);
            default:
                throw new IllegalStateException(ErrorMessages.UNSUPPORTED_PAYMENT_TYPE);
        }
    }

    private void validateDebtorAccount(AccountEntity debtor, Type accountIdentifierType)
            throws DebtorValidationException {
        AccountNumbersResponse accountNumbers = apiClient.getAccountNumbers();
        accountNumbers.checkIfTransactionTypeIsAllowed(debtor.getBban(), accountIdentifierType);
    }

    private PaymentResponse createCrossBorderPayment(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();
        final Creditor paymentCreditor = payment.getCreditor();
        final AccountIbanEntity creditor =
                new AccountIbanEntity(paymentCreditor.getAccountNumber(), payment.getCurrency());

        // Backwards compatibility patch: some agents would break if the dueDate was null, so we
        // defaulted it. This behaviour is no longer true for agents that properly implement the
        // execution of future dueDate. For more info about the fix, check PAY-549; for the support
        // of future dueDate, check PAY1-273.
        if (payment.getExecutionDate() == null) {
            payment.setExecutionDate(dateHelper.getNowAsLocalDate());
        }

        final CreditorAddressEntity creditorAddress =
                new CreditorAddressEntity(FormValues.CITY, FormValues.COUNTRY, FormValues.STREET);
        final AccountEntity debtor =
                new AccountEntity(payment.getDebtor().getAccountNumber(), payment.getCurrency());
        final AmountEntity amount = new AmountEntity(payment.getExactCurrencyAmount());
        final CrossBorderPaymentRequest crossBorderPaymentRequest =
                new CrossBorderPaymentRequest(
                        creditor,
                        creditorAddress,
                        paymentCreditor.getName(),
                        debtor,
                        amount,
                        FormKeys.SEPA,
                        payment.getExecutionDate()
                                .format(DateTimeFormatter.ofPattern(FormValues.DATE_FORMAT)));
        final CrossBorderPaymentResponse crossBorderPaymentResponse =
                apiClient.createCrossBorderPayment(crossBorderPaymentRequest);
        final GetPaymentStatusResponse getPaymentStatusResponse =
                apiClient.getPaymentStatus(
                        crossBorderPaymentResponse.getInitiatedCrossBorderPayment().getPaymentId());

        final PaymentStatus status =
                LansforsakringarConstants.PAYMENT_STATUS_MAPPER
                        .translate(getPaymentStatusResponse.getTransactionStatus())
                        .orElse(PaymentStatus.UNDEFINED);

        return crossBorderPaymentResponse.toTinkPayment(creditor, debtor, status);
    }

    private PaymentResponse createDomesticPayment(
            Payment payment, AccountEntity debtor, AmountEntity amount, String executionDate)
            throws PaymentException {

        AccountEntity creditor =
                new AccountEntity(payment.getCreditor().getAccountNumber(), amount.getCurrency());

        final DomesticPaymentRequest domesticPaymentRequest =
                new DomesticPaymentRequest(
                        creditor,
                        debtor,
                        amount,
                        executionDate,
                        Optional.ofNullable(payment.getRemittanceInformation())
                                .map(RemittanceInformation::getValue)
                                .orElse(null));

        DomesticPaymentResponse domesticPaymentResponse = new DomesticPaymentResponse();

        try {
            domesticPaymentResponse = apiClient.createDomesticPayment(domesticPaymentRequest);
        } catch (HttpResponseException ex) {
            HttpResponseExceptionHandler.checkForErrors(ex.getMessage());
        }

        final PaymentStatus status =
                LansforsakringarConstants.PAYMENT_STATUS_MAPPER
                        .translate(domesticPaymentResponse.getTransactionStatus())
                        .orElse(PaymentStatus.UNDEFINED);

        return domesticPaymentResponse.toTinkPayment(creditor, debtor, amount.toAmount(), status);
    }

    private PaymentResponse createDomesticGirosPayment(
            Payment payment, AccountEntity debtor, AmountEntity amount, String executionDate)
            throws PaymentException {

        GirosCreditorAccountEntity creditor =
                new GirosCreditorAccountEntity(
                        payment.getCreditor().getAccountIdentifier().getIdentifier(GIRO_FORMATTER),
                        payment.getCreditor().getAccountIdentifierType());

        final DomesticGirosPaymentRequest domesticGirosPaymentRequest =
                new DomesticGirosPaymentRequest(
                        creditor,
                        debtor,
                        amount,
                        executionDate,
                        Optional.ofNullable(payment.getRemittanceInformation())
                                .filter(
                                        r ->
                                                r.getType()
                                                        .equals(
                                                                RemittanceInformationType
                                                                        .UNSTRUCTURED))
                                .map(RemittanceInformation::getValue)
                                .orElse(null),
                        Optional.ofNullable(payment.getRemittanceInformation())
                                .filter(r -> r.getType().equals(RemittanceInformationType.OCR))
                                .map(RemittanceInformation::getValue)
                                .orElse(null));

        DomesticPaymentResponse domesticPaymentResponse = new DomesticPaymentResponse();

        try {
            domesticPaymentResponse =
                    apiClient.createDomesticGirosPayment(domesticGirosPaymentRequest);
        } catch (HttpResponseException ex) {
            HttpResponseExceptionHandler.checkForErrors(ex.getMessage());
        }

        final PaymentStatus status =
                LansforsakringarConstants.PAYMENT_STATUS_MAPPER
                        .translate(domesticPaymentResponse.getTransactionStatus())
                        .orElse(PaymentStatus.UNDEFINED);

        return domesticPaymentResponse.toTinkPayment(creditor, debtor, amount.toAmount(), status);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        final String paymentId = paymentRequest.getPayment().getUniqueId();
        final PaymentType type = paymentRequest.getPayment().getType();

        switch (type) {
            case DOMESTIC:
                return apiClient.getDomesticPayment(paymentId).toTinkPayment(paymentId);
            case SEPA:
                return apiClient.getCrossBorderPayment(paymentId).toTinkPayment(paymentId);
            default:
                throw new IllegalStateException(ErrorMessages.UNSUPPORTED_PAYMENT_TYPE);
        }
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        try {
            SignBasketResponse signBasketResponse =
                    signPayment(paymentMultiStepRequest.getPayment());

            if (!signBasketResponse.getScaStatus().equals(SCAValues.SCA_EXEMPTED)) {
                authenticatePIS(signBasketResponse);
            }
        } catch (HttpResponseException ex) {
            HttpResponseExceptionHandler.checkForErrors(ex.getMessage());
        }

        PaymentResponse currentState = fetchAndValidatePayment(paymentMultiStepRequest);

        return new PaymentMultiStepResponse(
                currentState.getPayment(),
                AuthenticationStepConstants.STEP_FINALIZE,
                new ArrayList<>());
    }

    private PaymentResponse fetchAndValidatePayment(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        PaymentResponse paymentResponse =
                fetch(
                        new PaymentRequest(
                                paymentMultiStepRequest.getPayment(),
                                Storage.copyOf(paymentMultiStepRequest.getStorage())));

        PaymentStatus paymentStatus = paymentResponse.getPayment().getStatus();

        if (paymentStatus == PaymentStatus.PENDING || paymentStatus == PaymentStatus.CANCELLED) {
            throw new PaymentCancelledException();
        }

        if (paymentStatus == PaymentStatus.REJECTED) {
            throw new PaymentValidationException("Payment rejected by the bank.");
        }

        if (paymentStatus != PaymentStatus.SIGNED) {
            throw new PaymentRejectedException("Unexpected payment status: " + paymentStatus);
        }

        return paymentResponse;
    }

    private SignBasketResponse signPayment(Payment payment) {
        CreateBasketResponse signingBasket = apiClient.createSigningBasket(payment.getUniqueId());

        String authorizationUrl = signingBasket.getLinks().getAuthorizationUrl();

        return apiClient.signBasket(authorizationUrl, signingBasket.getBasketId());
    }

    private void authenticatePIS(SignBasketResponse signBasketResponse) {
        URL url =
                apiClient.buildAuthorizeUrl(
                        strongAuthenticationState.getState(),
                        signBasketResponse.getAuthorisationId());

        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(url));

        supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);
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
        return paymentListRequest.getPaymentRequestList().stream()
                .map(this::fetch)
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(), PaymentListResponse::new));
    }
}
