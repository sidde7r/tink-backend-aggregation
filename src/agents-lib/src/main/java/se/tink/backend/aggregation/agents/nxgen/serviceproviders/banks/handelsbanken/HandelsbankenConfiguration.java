package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.HandelsbankenLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.rpc.Credentials;

public interface HandelsbankenConfiguration<API extends HandelsbankenApiClient> {

    String getAppId();

    URL getEntryPoint();

    String getAppVersion();

    String getAuthTp();

    AuthorizeResponse toAuthorized(ValidateSignatureResponse validateSignature,
            Credentials credentials,
            API client) throws SessionException;

    Class<? extends AccountListResponse> getAccountListResponse();

    Class<? extends TransactionsResponse> getTransactionsReponse();

    URL toCards(ApplicationEntryPointResponse applicationEntryPoint);

    Class<? extends CreditCardsResponse> getCreditCardsResponse();

    Class<? extends HandelsbankenLoansResponse> getLoansResponse();
}
