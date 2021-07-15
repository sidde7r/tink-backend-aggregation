package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants.URLS.Parameters.GIRO_NUMBER;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.httpclient.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.device.CheckAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.PendingTransaction;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.BaseSignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ConfirmInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.HandelsbankenSETransferContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferApprovalRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ValidateRecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ValidateRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardSETransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionFund;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionInfo;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.SecurityHolding;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.SecurityHoldingContainer;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.CustodyAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.FundHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.HandelsbankenSEFundAccountHoldingDetail;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.PensionDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.PensionOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.SecurityHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.PaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PaymentRecipient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PendingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CommitProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CreateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.EncryptedUserCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.InitNewProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

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

    public InitBankIdResponse initToBank(InitBankIdRequest initBankIdRequest) {
        return createPostRequest(HandelsbankenSEConstants.Urls.INIT_REQUEST)
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
                .getFundHoldingsUrl()
                .map(url -> createRequest(url).get(FundHoldingsResponse.class));
    }

    public Optional<CustodyAccountResponse> custodyAccount(CustodyAccount custodyAccount) {
        return custodyAccount
                .getCustodyAccountUrl()
                .map(url -> createRequest(url).get(CustodyAccountResponse.class));
    }

    public PensionOverviewResponse pensionOverview(
            ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toPensionOverview())
                .get(PensionOverviewResponse.class);
    }

    public PensionDetailsResponse pensionDetails(HandelsbankenSEPensionInfo pensionInfo) {
        return createRequest(pensionInfo.toPensionDetail()).get(PensionDetailsResponse.class);
    }

    public Optional<PensionDetailsResponse> pensionDetails(CustodyAccount custodyAccount) {
        return custodyAccount
                .getPensionDetailsUrl()
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

    public Optional<HandelsbankenSEFundAccountHoldingDetail> fundHoldingDetail(
            SecurityHolding fund) {
        return fund.toFundHoldingDetail()
                .map(url -> createRequest(url).get(HandelsbankenSEFundAccountHoldingDetail.class));
    }

    public HandelsbankenSETransferContext transferContext(
            ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toTransferContext())
                .get(HandelsbankenSETransferContext.class);
    }

    public TransferSignResponse signTransfer(URL requestUrl, BaseSignRequest request) {
        TransferSignResponse transferSignResponse = null;

        try {
            transferSignResponse =
                    createPostRequest(requestUrl).post(TransferSignResponse.class, request);
        } catch (HttpResponseException exception) {
            if (exception.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                transferSignResponse = exception.getResponse().getBody(TransferSignResponse.class);
            } else {
                throw exception;
            }
        }

        return transferSignResponse;
    }

    public ConfirmInfoResponse getConfirmInfo(URL url) {
        ConfirmInfoResponse response;
        try {
            response = createRequest(url).get(ConfirmInfoResponse.class);
        } catch (HttpResponseException exception) {
            if (exception.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                // done for COB-758.
                response = exception.getResponse().getBody(ConfirmInfoResponse.class);
            } else {
                throw exception;
            }
        }
        return response;
    }

    public ConfirmTransferResponse postConfirmTransfer(URL url) {
        return createPostRequest(url).post(ConfirmTransferResponse.class);
    }

    public TransferApprovalResponse postApproveTransfer(
            URL url, TransferApprovalRequest transferApprovalRequest) {
        try {
            return createPostRequest(url)
                    .post(TransferApprovalResponse.class, transferApprovalRequest);
        } catch (HttpResponseException e) {
            return e.getResponse().getBody(TransferApprovalResponse.class);
        }
    }

    public ValidateRecipientResponse validateRecipient(
            HandelsbankenSETransferContext transferContext,
            ValidateRecipientRequest validateRecipient) {

        try {
            return createPostRequest(transferContext.toValidateRecipient())
                    .post(ValidateRecipientResponse.class, validateRecipient);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();

            if (response.getStatus() == HttpStatus.SC_BAD_REQUEST) {
                return response.getBody(ValidateRecipientResponse.class);
            }

            throw e;
        }
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
}
