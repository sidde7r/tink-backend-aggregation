package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.DeviceAuthentication;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.rpc.AccountListSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.SignatureValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.loan.rpc.HandelsbankenLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class HandelsbankenSEConfiguration
        implements HandelsbankenConfiguration<HandelsbankenSEApiClient> {

    @Override
    public String getAppId() {
        return DeviceAuthentication.APP_ID;
    }

    @Override
    public URL getEntryPoint() {
        return Urls.ENTRY_POINT;
    }

    @Override
    public String getAppVersion() {
        return Headers.APP_VERSION;
    }

    @Override
    public String getDeviceModel() {
        return Headers.DEVICE_MODEL;
    }

    @Override
    public String getAuthTp() {
        return DeviceAuthentication.AUTH_TP;
    }

    @Override
    public AuthorizeResponse toAuthorized(
            ValidateSignatureResponse validateSignature,
            Credentials credentials,
            HandelsbankenSEApiClient client)
            throws SessionException {
        new SignatureValidator(
                        validateSignature,
                        HandelsbankenSEConstants.DeviceAuthentication.VALID_SIGNATURE_RESULT,
                        credentials)
                .validate();
        return client.authorize(validateSignature);
    }

    @Override
    public Class<? extends AccountListResponse> getAccountListResponse() {
        return AccountListSEResponse.class;
    }

    public Class<? extends AccountListSEResponse> getAccountListSEResponse() {
        return AccountListSEResponse.class;
    }

    @Override
    public Class<? extends TransactionsResponse> getTransactionsReponse() {
        return TransactionsSEResponse.class;
    }

    @Override
    public URL toCards(ApplicationEntryPointResponse applicationEntryPoint) {
        return null;
    }

    @Override
    public Class<? extends CreditCardsResponse> getCreditCardsResponse() {
        return null;
    }

    @Override
    public Class<? extends HandelsbankenLoansResponse> getLoansResponse() {
        return null;
    }
}
