package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.AccountListSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.CreditCardsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.LoansSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.SignatureValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.HandelsbankenLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.rpc.Credentials;

public class HandelsbankenSEConfiguration implements HandelsbankenConfiguration<HandelsbankenSEApiClient> {

    @Override
    public String getAppId() {
        return HandelsbankenSEConstants.DeviceAuthentication.APP_ID;

    }

    @Override
    public URL getEntryPoint() {
        return HandelsbankenSEConstants.Urls.ENTRY_POINT;
    }

    @Override
    public String getAppVersion() {
        return HandelsbankenSEConstants.Headers.APP_VERSION;

    }

    @Override
    public String getAuthTp() {
        return HandelsbankenSEConstants.DeviceAuthentication.AUTH_TP;
    }

    @Override
    public AuthorizeResponse toAuthorized(ValidateSignatureResponse validateSignature, Credentials credentials,
            HandelsbankenSEApiClient client) throws SessionException {
        new SignatureValidator(validateSignature, HandelsbankenSEConstants.DeviceAuthentication.VALID_SIGNATURE_RESULT, credentials).validate();
        return client.authorize(validateSignature);
    }

    @Override
    public Class<? extends AccountListSEResponse> getAccountListResponse() {
        return AccountListSEResponse.class;
    }

    @Override
    public Class<? extends TransactionsResponse> getTransactionsReponse() {
        return TransactionsSEResponse.class;
    }

    @Override
    public URL toCards(ApplicationEntryPointResponse applicationEntryPoint) {
        return applicationEntryPoint.toCardsV3();
    }

    @Override
    public Class<? extends CreditCardsResponse> getCreditCardsResponse() {
        return CreditCardsSEResponse.class;
    }

    @Override
    public Class<? extends HandelsbankenLoansResponse> getLoansResponse() {
        return LoansSEResponse.class;
    }
}
