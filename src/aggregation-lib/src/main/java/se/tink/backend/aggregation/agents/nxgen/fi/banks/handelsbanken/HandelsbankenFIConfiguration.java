package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.rpc.AccountListFIResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.rpc.CreditCardsFIResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.rpc.LoansFIResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.rpc.TransactionsFIResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.SignatureValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.HandelsbankenLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.rpc.Credentials;

public class HandelsbankenFIConfiguration implements HandelsbankenConfiguration {

    @Override
    public String getAppId() {
        return HandelsbankenFIConstants.DeviceAuthentication.APP_ID;
    }

    @Override
    public URL getEntryPoint() {
        return HandelsbankenFIConstants.Urls.ENTRY_POINT;
    }

    @Override
    public String getAppVersion() {
        return HandelsbankenFIConstants.Headers.APP_VERSION;
    }

    @Override
    public String getAuthTp() {
        return HandelsbankenFIConstants.DeviceAuthentication.AUTH_TP;
    }

    @Override
    public AuthorizeResponse toAuthorized(ValidateSignatureResponse validateSignature,
            Credentials credentials,
            HandelsbankenApiClient client) throws SessionException {
        new SignatureValidator(validateSignature, HandelsbankenFIConstants.DeviceAuthentication.VALID_SIGNATURE_RESULT, credentials).validate();
        AuthorizeResponse authorize = new AuthorizeResponse();
        authorize.setLinks(validateSignature.getLinks());
        return authorize;
    }

    @Override
    public Class<? extends AccountListResponse> getAccountListResponse() {
        return AccountListFIResponse.class;
    }

    @Override
    public Class<? extends TransactionsResponse> getTransactionsReponse() {
        return TransactionsFIResponse.class;
    }

    @Override
    public URL toCards(ApplicationEntryPointResponse applicationEntryPoint) {
        return applicationEntryPoint.toCards();
    }

    @Override
    public Class<? extends CreditCardsResponse> getCreditCardsResponse() {
        return CreditCardsFIResponse.class;
    }

    @Override
    public Class<? extends HandelsbankenLoansResponse> getLoansResponse() {
        return LoansFIResponse.class;
    }
}
