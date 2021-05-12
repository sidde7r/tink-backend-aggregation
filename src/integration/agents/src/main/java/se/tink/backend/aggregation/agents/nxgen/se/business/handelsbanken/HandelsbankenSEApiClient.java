package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.AuthorizeMandateRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.rpc.AccountListSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class HandelsbankenSEApiClient extends HandelsbankenApiClient {

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

    public AuthorizeResponse authorize(ValidateSignatureResponse validateSignature) {
        return createPostRequest(validateSignature.toAuthorize()).post(AuthorizeResponse.class);
    }

    public AuthorizeResponse authorize(AuthenticateResponse authenticate) {
        return createPostRequest(authenticate.toAuthorize()).post(AuthorizeResponse.class);
    }

    public AuthorizeResponse authorizeMandate(
            AuthorizeResponse authorizeResponse, AuthorizeMandateRequest authorizeMandateRequest) {
        return createPostRequest(authorizeResponse.toAuthorizeMandate())
                .post(AuthorizeResponse.class, authorizeMandateRequest);
    }

    public AccountListSEResponse accountList(ApplicationEntryPointResponse applicationEntryPoint) {
        return (AccountListSEResponse)
                createRequest(applicationEntryPoint.toAccounts())
                        .get(handelsbankenConfiguration.getAccountListResponse());
    }

    @Override
    public TransactionsSEResponse transactions(HandelsbankenAccount handelsbankenAccount) {
        return createRequest(handelsbankenAccount.getAccountTransactionsUrl())
                .get(TransactionsSEResponse.class);
    }

    @Override
    public <CreditCard extends HandelsbankenCreditCard>
            CreditCardTransactionsResponse creditCardTransactions(CreditCard creditcard) {
        return null;
    }

    @Override
    public <CreditCard extends HandelsbankenCreditCard>
            CreditCardTransactionsResponse<CreditCard> creditCardTransactions(URL url) {
        return null;
    }
}
