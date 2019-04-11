package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.EncryptedSecurityCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.SecurityCardResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.VerifySecurityCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardFITransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardsFIResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsFIResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CreateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.EncryptedUserCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.InitNewProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenFIApiClient extends HandelsbankenApiClient {

    public HandelsbankenFIApiClient(
            TinkHttpClient client, HandelsbankenFIConfiguration handelsbankenConfiguration) {
        super(client, handelsbankenConfiguration);
    }

    public SecurityCardResponse authenticate(
            InitNewProfileResponse initNewProfile,
            EncryptedUserCredentialsRequest encryptedUserCredentialsRequest) {
        return createPostRequest(initNewProfile.toAuthenticate())
                .post(SecurityCardResponse.class, encryptedUserCredentialsRequest);
    }

    public VerifySecurityCodeResponse verifySecurityCode(
            SecurityCardResponse authenticate, EncryptedSecurityCodeRequest code) {
        return createPostRequest(authenticate.toVerifySecurityCode())
                .post(VerifySecurityCodeResponse.class, code);
    }

    public CreateProfileResponse createProfile(VerifySecurityCodeResponse verifySecurityCode) {
        return createRequest(verifySecurityCode.toCreateProfile()).get(CreateProfileResponse.class);
    }

    public CreditCardsFIResponse creditCards(ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(handelsbankenConfiguration.toCards(applicationEntryPoint))
                .get(CreditCardsFIResponse.class);
    }

    @Override
    public TransactionsFIResponse transactions(HandelsbankenAccount handelsbankenAccount) {
        return createRequest(handelsbankenAccount.getAccountTransactionsUrl())
                .get(TransactionsFIResponse.class);
    }

    public TransactionsFIResponse transactions(URL url) {
        return createRequest(url).get(TransactionsFIResponse.class);
    }

    @Override
    public CreditCardFITransactionsResponse creditCardTransactions(
            HandelsbankenCreditCard creditcard) {
        return createRequest(creditcard.getCardTransactionsUrl())
                .get(CreditCardFITransactionsResponse.class);
    }

    @Override
    public CreditCardFITransactionsResponse creditCardTransactions(URL url) {
        return createRequest(url).get(CreditCardFITransactionsResponse.class);
    }
}
