package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.EncryptedSecurityCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.SecurityCardResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.VerifySecurityCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.rpc.CreditCardFITransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CreateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.EncryptedUserCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.InitNewProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class HandelsbankenFIApiClient extends HandelsbankenApiClient {
    public HandelsbankenFIApiClient(TinkHttpClient client, HandelsbankenFIConfiguration handelsbankenConfiguration) {
        super(client, handelsbankenConfiguration);
    }

    public SecurityCardResponse authenticate(InitNewProfileResponse initNewProfile,
            EncryptedUserCredentialsRequest encryptedUserCredentialsRequest) {
        return createPostRequest(initNewProfile.toAuthenticate()).post(SecurityCardResponse.class,
                encryptedUserCredentialsRequest);
    }

    public VerifySecurityCodeResponse verifySecurityCode(SecurityCardResponse authenticate,
            EncryptedSecurityCodeRequest code) {
        return createPostRequest(authenticate.toVerifySecurityCode()).post(VerifySecurityCodeResponse.class, code);
    }

    public CreateProfileResponse createProfile(VerifySecurityCodeResponse verifySecurityCode) {
        return createRequest(verifySecurityCode.toCreateProfile()).get(CreateProfileResponse.class);
    }

    @Override
    public <CreditCard extends HandelsbankenCreditCard> CreditCardFITransactionsResponse creditCardTransactions(
            CreditCard creditcard) {
        return createRequest(creditcard.toCardTransactions()).get(CreditCardFITransactionsResponse.class);
    }
}
