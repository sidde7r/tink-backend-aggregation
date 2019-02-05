package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.device.CheckAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.EInvoice;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.PendingTransaction;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc.ApproveEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc.ApproveEInvoiceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc.EInvoiceDetails;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc.SignEInvoicesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.UpdatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.HandelsbankenSETransferContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSpecificationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSpecificationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ValidateRecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ValidateRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardSETransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.einvoice.rpc.PendingEInvoicesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionFund;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.SecurityHolding;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.SecurityHoldingContainer;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.CustodyAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.FundHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.HandelsbankenSEFundAccountHoldingDetail;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.PensionDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.SecurityHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.PaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.interfaces.UpdatablePayment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PaymentRecipient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PendingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.EntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CommitProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CreateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.EncryptedUserCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.InitNewProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants.URLS.Parameters.GIRO_NUMBER;

public class HandelsbankenSEApiClient extends HandelsbankenApiClient {

    // local cache for transactions response since SHB has changed their tx-fetching
    // we get all tx in one request today, no need to paginate since this is all we get
    // the url as String is the key
    private final Map<String, TransactionsSEResponse> transactionsCache = new HashMap<>();
    private final Map<String, CreditCardSETransactionsResponse> creditCardTransactionsCache =
            new HashMap<>();
    private final Map<String, PendingTransactionsResponse> pendingTransactionsCache =
            new HashMap<>();

    public HandelsbankenSEApiClient(
            TinkHttpClient client, HandelsbankenSEConfiguration configuration) {
        super(client, configuration);
    }

    public InitBankIdResponse initBankId(
            EntryPointResponse entryPoint, InitBankIdRequest initBankIdRequest) {
        return createPostRequest(entryPoint.toBankIdLogin())
                .post(InitBankIdResponse.class, initBankIdRequest);
    }

    public AuthenticateResponse authenticate(InitBankIdResponse initBankId) {
        return createPostRequest(initBankId.toAuthenticate()).post(AuthenticateResponse.class);
    }

    public AuthorizeResponse authorize(AuthenticateResponse authenticate) {
        return createPostRequest(authenticate.toAuthorize()).post(AuthorizeResponse.class);
    }

    public CreateProfileResponse createProfile(
            InitNewProfileResponse initNewProfile,
            EncryptedUserCredentialsRequest encryptedUserCredentialsRequest) {
        return createPostRequest(initNewProfile.toCreateProfile())
                .post(CreateProfileResponse.class, encryptedUserCredentialsRequest);
    }

    public CheckAgreementResponse checkAgreement(CommitProfileResponse commitProfile) {
        return createRequest(commitProfile.toCheckAgreement()).get(CheckAgreementResponse.class);
    }

    public AuthorizeResponse authorize(ValidateSignatureResponse validateSignature) {
        return createPostRequest(validateSignature.toAuthorize()).post(AuthorizeResponse.class);
    }

    @Override
    public TransactionsSEResponse transactions(HandelsbankenAccount account) {

        String txUrl = account.getAccountTransactionsUrl().get();
        if (transactionsCache.containsKey(txUrl)) {
            return transactionsCache.get(txUrl);
        }

        TransactionsSEResponse txResponse =
                createRequest(account.getAccountTransactionsUrl())
                        .get(TransactionsSEResponse.class);
        transactionsCache.put(txUrl, txResponse);

        return txResponse;
    }

    public PendingTransactionsResponse pendingTransactions(
            ApplicationEntryPointResponse applicationEntryPoint) {
        URL pendingUrl = applicationEntryPoint.toPendingTransactions();
        String txUrl = pendingUrl.get();

        if (pendingTransactionsCache.containsKey(txUrl)) {
            return pendingTransactionsCache.get(txUrl);
        }

        PendingTransactionsResponse txResponse =
                createRequest(pendingUrl).get(PendingTransactionsResponse.class);
        pendingTransactionsCache.put(txUrl, txResponse);

        return txResponse;
    }

    public PendingEInvoicesResponse pendingEInvoices(
            ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toPendingEInvoices())
                .get(PendingEInvoicesResponse.class);
    }

    public Optional<EInvoiceDetails> eInvoiceDetails(EInvoice eInvoice) {
        return eInvoice.toEInvoiceDetails()
                .map(url -> createRequest(url).get(EInvoiceDetails.class));
    }

    public Optional<ApproveEInvoiceResponse> approveEInvoice(
            EInvoiceDetails eInvoiceDetails, ApproveEInvoiceRequest request) {
        return eInvoiceDetails
                .toApproval()
                .map(url -> createPostRequest(url).post(ApproveEInvoiceResponse.class, request));
    }

    public Optional<SignEInvoicesResponse> signEInvoice(
            ApproveEInvoiceResponse approveEInvoiceResponse) {
        return approveEInvoiceResponse
                .toSignature()
                .map(url -> createPostRequest(url).post(SignEInvoicesResponse.class));
    }

    public HandelsbankenSEPaymentContext paymentContext(UpdatablePayment updatablePayment) {
        return createRequest(updatablePayment.toPaymentContext())
                .get(HandelsbankenSEPaymentContext.class);
    }

    public Optional<PaymentDetails> createPayment(
            HandelsbankenSEPaymentContext paymentContext, CreatePaymentRequest request) {
        return paymentContext
                .toCreate()
                .map(url -> createPostRequest(url).post(PaymentDetails.class, request));
    }

    public Optional<UpdatablePayment> updatePayment(
            UpdatablePayment updatablePayment, UpdatePaymentRequest request) {
        return updatablePayment
                .toUpdate()
                .map(url -> createPostRequest(url).put(updatablePayment.getClass(), request));
    }

    public Optional<PaymentDetails> signPayment(PaymentDetails paymentDetails) {
        return paymentDetails
                .toSignature()
                .map(url -> createPostRequest(url).post(PaymentDetails.class));
    }

    public CreditCardsSEResponse creditCards(ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(handelsbankenConfiguration.toCards(applicationEntryPoint))
                .get(CreditCardsSEResponse.class);
    }

    @Override
    public CreditCardSETransactionsResponse creditCardTransactions(
            HandelsbankenCreditCard creditCard) {

        String txUrl = creditCard.getCardTransactionsUrl().get();
        if (creditCardTransactionsCache.containsKey(txUrl)) {
            return creditCardTransactionsCache.get(txUrl);
        }

        CreditCardSETransactionsResponse txResponse =
                creditCardTransactions(creditCard.getCardTransactionsUrl());
        creditCardTransactionsCache.put(txUrl, txResponse);

        return txResponse;
    }

    @Override
    public CreditCardSETransactionsResponse creditCardTransactions(URL url) {
        return createRequest(url).get(CreditCardSETransactionsResponse.class);
    }

    public Optional<PaymentDetails> paymentDetails(PendingTransaction pendingTransaction) {
        return pendingTransaction
                .toPaymentDetails()
                .map(url -> createRequest(url).get(PaymentDetails.class));
    }

    public SecurityHoldingsResponse securitiesHoldings(
            ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toSecuritiesHoldings())
                .get(SecurityHoldingsResponse.class);
    }

    public Optional<FundHoldingsResponse> fundHoldings(CustodyAccount custodyAccount) {
        return custodyAccount
                .toFundHoldings()
                .map(url -> createRequest(url).get(FundHoldingsResponse.class));
    }

    public Optional<CustodyAccountResponse> custodyAccount(CustodyAccount custodyAccount) {
        return custodyAccount
                .toCustodyAccount()
                .map(url -> createRequest(url).get(CustodyAccountResponse.class));
    }

    public Optional<PensionDetailsResponse> pensionDetails(CustodyAccount custodyAccount) {
        return custodyAccount
                .toPensionDetails()
                .map(url -> createRequest(url).get(PensionDetailsResponse.class));
    }

    public Optional<SecurityHoldingContainer> securityHolding(SecurityHolding securityHolding) {
        return securityHolding
                .toSecurityHolding()
                .map(url -> createRequest(url).get(SecurityHoldingContainer.class));
    }

    public Optional<HandelsbankenSEFundAccountHoldingDetail> fundHoldingDetail(
            HandelsbankenSEPensionFund pensionFund) {
        return pensionFund
                .toFundHoldingDetail()
                .map(url -> createRequest(url).get(HandelsbankenSEFundAccountHoldingDetail.class));
    }

    public HandelsbankenSETransferContext transferContext(
            ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toTransferContext())
                .get(HandelsbankenSETransferContext.class);
    }

    public TransferSpecificationResponse createTransfer(
            Creatable creatable, TransferSpecificationRequest transferSpecification) {
        try {
            return createPostRequest(creatable.toCreate())
                    .post(TransferSpecificationResponse.class, transferSpecification);
        } catch (HttpResponseException e) {
            // Still interested in the deserialized response.
            return e.getResponse().getBody(TransferSpecificationResponse.class);
        }
    }

    public TransferSignatureResponse signTransfer(Signable signable) {
        return createPostRequest(signable.toSignature()).post(TransferSignatureResponse.class);
    }

    public ValidateRecipientResponse validateRecipient(
            HandelsbankenSETransferContext transferContext,
            ValidateRecipientRequest validateRecipient) {
        return createPostRequest(transferContext.toValidateRecipient())
                .post(ValidateRecipientResponse.class, validateRecipient);
    }

    public PaymentRecipient lookupRecipient(
            HandelsbankenSEPaymentContext paymentContext, String giroNumberFormatted) {
        return createRequest(
                        paymentContext
                                .toLookupRecipient()
                                .parameter(GIRO_NUMBER, giroNumberFormatted))
                .get(PaymentRecipient.class);
    }

    public HandelsbankenSEPaymentContext paymentContext(
            ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toPaymentContext())
                .get(HandelsbankenSEPaymentContext.class);
    }

    public interface Creatable {
        URL toCreate();
    }

    public interface Signable {
        URL toSignature();
    }
}
