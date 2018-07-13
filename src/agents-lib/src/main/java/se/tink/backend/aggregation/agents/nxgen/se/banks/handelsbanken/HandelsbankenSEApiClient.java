package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.device.CheckAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.HandelsbankenSETransferContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.TransferSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.TransferSpecificationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.TransferSpecificationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.ValidateRecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.ValidateRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSEPensionFund;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.PendingTransaction;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.SecurityHolding;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.SecurityHoldingContainer;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.CreditCardSETransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.CustodyAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.FundHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.HandelsbankenSEFundAccountHoldingDetail;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.PaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.PendingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.PensionDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.SecurityHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.EntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CommitProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CreateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.EncryptedUserCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.InitNewProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class HandelsbankenSEApiClient extends HandelsbankenApiClient {

    public HandelsbankenSEApiClient(TinkHttpClient client, HandelsbankenSEConfiguration configuration) {
        super(client, configuration);
    }

    public InitBankIdResponse initBankId(
            EntryPointResponse entryPoint,
            InitBankIdRequest initBankIdRequest) {
        return createPostRequest(entryPoint.toBankIdLogin()).post(InitBankIdResponse.class, initBankIdRequest);
    }

    public AuthenticateResponse authenticate(InitBankIdResponse initBankId) {
        return createPostRequest(initBankId.toAuthenticate()).post(AuthenticateResponse.class);
    }

    public AuthorizeResponse authorize(AuthenticateResponse authenticate) {
        return createPostRequest(authenticate.toAuthorize()).post(AuthorizeResponse.class);
    }

    public CreateProfileResponse createProfile(InitNewProfileResponse initNewProfile,
            EncryptedUserCredentialsRequest encryptedUserCredentialsRequest) {
        return createPostRequest(initNewProfile.toCreateProfile()).post(CreateProfileResponse.class,
                encryptedUserCredentialsRequest);
    }

    public CheckAgreementResponse checkAgreement(CommitProfileResponse commitProfile) {
        return createRequest(commitProfile.toCheckAgreement()).get(CheckAgreementResponse.class);
    }

    public AuthorizeResponse authorize(ValidateSignatureResponse validateSignature) {
        return createPostRequest(validateSignature.toAuthorize()).post(AuthorizeResponse.class);
    }

    public PendingTransactionsResponse pendingTransactions(
            ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toPendingTransactions()).get(PendingTransactionsResponse.class);
    }

    @Override
    public CreditCardSETransactionsResponse creditCardTransactions(HandelsbankenCreditCard creditCard) {
        return createRequest(creditCard.toCardTransactions()).get(CreditCardSETransactionsResponse.class);
    }

    public Optional<PaymentDetails> paymentDetails(PendingTransaction pendingTransaction) {
        return pendingTransaction.paymentDetails().map(url -> createRequest(url).get(PaymentDetails.class));
    }

    public SecurityHoldingsResponse securitiesHoldings(ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toSecuritiesHoldings()).get(SecurityHoldingsResponse.class);
    }

    public Optional<FundHoldingsResponse> fundHoldings(CustodyAccount custodyAccount) {
        return custodyAccount.toFundHoldings().map(url -> createRequest(url).get(FundHoldingsResponse.class));
    }

    public Optional<CustodyAccountResponse> custodyAccount(CustodyAccount custodyAccount) {
        return custodyAccount.toCustodyAccount().map(url -> createRequest(url).get(CustodyAccountResponse.class));
    }

    public Optional<PensionDetailsResponse> pensionDetails(CustodyAccount custodyAccount) {
        return custodyAccount.toPensionDetails().map(url -> createRequest(url).get(PensionDetailsResponse.class));
    }

    public Optional<SecurityHoldingContainer> securityHolding(SecurityHolding securityHolding) {
        return securityHolding.toSecurityHolding().map(url -> createRequest(url).get(SecurityHoldingContainer.class));
    }

    public Optional<HandelsbankenSEFundAccountHoldingDetail> fundHoldingDetail(HandelsbankenSEPensionFund pensionFund) {
        return pensionFund.toFundHoldingDetail().map(url ->
                createRequest(url).get(HandelsbankenSEFundAccountHoldingDetail.class));
    }

    public HandelsbankenSETransferContext transferContext(ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toTransferContext()).get(HandelsbankenSETransferContext.class);
    }

    public TransferSpecificationResponse createTransfer(Creatable creatable,
            TransferSpecificationRequest transferSpecification) {
        try {
            return createPostRequest(creatable.toCreate())
                    .post(TransferSpecificationResponse.class, transferSpecification);
        } catch (HttpResponseException e) {
            // Still interested in the deserialized response.
            return e.getResponse().getBody(TransferSpecificationResponse.class);
        }
    }

    public TransferSignatureResponse signTransfer(Signable signable) {
        return createPostRequest(signable.toSignature())
                .post(TransferSignatureResponse.class);
    }

    public ValidateRecipientResponse validateRecipient(HandelsbankenSETransferContext transferContext,
            ValidateRecipientRequest validateRecipient) {
        return createPostRequest(transferContext.toValidateRecipient())
                .post(ValidateRecipientResponse.class, validateRecipient);
    }

    public HandelsbankenSEPaymentContext paymentContext(ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toPaymentContext()).get(HandelsbankenSEPaymentContext.class);
    }

    public interface Creatable {
        URL toCreate();
    }

    public interface Signable {
        URL toSignature();
    }
}
