package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.RequestValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentAuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.StatementResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.entities.SwedbankBalticsAccessEntity;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SwedbankBalticsApiClient extends SwedbankApiClient {

    public SwedbankBalticsApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<SwedbankConfiguration> agentConfiguration,
            QsealcSigner qsealcSigner,
            AgentComponentProvider componentProvider,
            String bic,
            String authenticationMethodId,
            String bookingStatus) {
        super(
                client,
                persistentStorage,
                agentConfiguration,
                qsealcSigner,
                componentProvider,
                bic,
                authenticationMethodId,
                bookingStatus);
    }

    public AuthenticationResponse authenticateDecoupledBaltics(String ssn, String personalId) {

        AuthorizeRequest.AuthorizeRequestBuilder requestBuilder =
                AuthorizeRequest.builder()
                        .clientID(configuration.getClientId())
                        .redirectUri(getRedirectUrl())
                        .authenticationMethodId(authenticationMethodId)
                        .personalID(personalId)
                        .scope(RequestValues.ALL_ACCOUNTS_SCOPES);

        return createRequest(SwedbankConstants.Urls.AUTHORIZATION_DECOUPLED)
                .header(HeaderKeys.PSU_ID, ssn)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(AuthenticationResponse.class, requestBuilder.build());
    }

    // additional request for detailed consent for EE / user needs to sign with PIN2 via Smart ID.
    // Looks like the same for LV and LT
    public AuthenticationResponse authorizeConsent(String url) {
        return createRequestInSession(new URL(Urls.BASE.concat(url)), true)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(
                        AuthenticationResponse.class,
                        new ConsentAuthorizeRequest(authenticationMethodId));
    }

    @Override
    public Optional<StatementResponse> postOrGetOfflineStatement(
            String accountId, LocalDate fromDate, LocalDate toDate) {

        // Swedbank doesn't allow offline statement without PSU involvement
        if (componentProvider.getCredentialsRequest().getUserAvailability().isUserPresent()) {
            RequestBuilder requestBuilder =
                    createRequestInSession(
                                    Urls.ACCOUNT_TRANSACTIONS.parameter(
                                            UrlParameters.ACCOUNT_ID, accountId),
                                    true)
                            .queryParam(SwedbankConstants.HeaderKeys.FROM_DATE, fromDate.toString())
                            .queryParam(SwedbankConstants.HeaderKeys.TO_DATE, toDate.toString())
                            .queryParam(SwedbankConstants.QueryKeys.BOOKING_STATUS, bookingStatus)
                            .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                            .header(HeaderKeys.TPP_NOK_REDIRECT_URI, getRedirectUrl());

            try {
                return Optional.of(requestBuilder.post(StatementResponse.class));
            } catch (HttpResponseException hre) {
                GenericResponse errorResponse = hre.getResponse().getBody(GenericResponse.class);
                if (errorResponse.isResourceAlreadySigned()) {
                    return Optional.of(requestBuilder.get(StatementResponse.class));
                }
                throw new IllegalStateException(hre);
            }
        }
        return Optional.empty();
    }

}
