package se.tink.backend.aggregation.agents.standalone.mapper.factory;

import se.tink.backend.aggregation.agents.standalone.mapper.auth.agg.ThirdPartyAppAuthenticationPayloadMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.auth.sa.AuthenticationRequestMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.agg.AuthenticationResponseMappersFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.agg.FetchAccountsResponseMapperFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.agg.FetchTransactionsResponseMapperFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.sa.AuthenticationRequestMapperFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.sa.CheckingRequestMappersFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.sa.CommonMappersFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.sa.FetchTransactionsRequestMappersFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.FetchAccountsResponseMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.sa.FetchAccountsRequestMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg.FetchTransactionsResponseMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.sa.FetchTransactionsRequestMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.providers.CommonExternalParametersProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * This class is to keep control on number of mappers and have all required factory method in it. It
 * also supports lifecycle of states required for calls of agent.
 */
public class MappersController {

    private final CommonMappersFactory commonMappersFactory;

    private AuthenticationRequestMapper authenticationRequestMapper;
    private ThirdPartyAppAuthenticationPayloadMapper thirdPartyAppAuthenticationPayloadMapper;

    private FetchAccountsResponseMapper fetchAccountsResponseMapper;
    private FetchAccountsRequestMapper fetchAccountsRequestMapper;

    private FetchTransactionsRequestMapper fetchTransactionsRequestMapper;
    private FetchTransactionsResponseMapper fetchTransactionsResponseMapper;

    private final AuthenticationRequestMapperFactory authenticationRequestMapperFactory;
    private final AuthenticationResponseMappersFactory authenticationResponseMappersFactory;

    private final CheckingRequestMappersFactory checkingRequestMappersFactory;
    private final FetchAccountsResponseMapperFactory fetchAccountsResponseMapperFactory;

    private final FetchTransactionsRequestMappersFactory fetchTransactionsRequestsFactory;
    private final FetchTransactionsResponseMapperFactory fetchTransactionsResponseMapperFactory;

    private MappersController(
            CommonExternalParametersProvider commonExternalParametersProvider,
            CredentialsRequest request) {
        commonMappersFactory =
                CommonMappersFactory.newInstance(
                        commonExternalParametersProvider, request.isManual());

        authenticationRequestMapperFactory =
                AuthenticationRequestMapperFactory.newInstance(commonMappersFactory);
        authenticationResponseMappersFactory = AuthenticationResponseMappersFactory.newInstance();

        fetchAccountsResponseMapperFactory =
                FetchAccountsResponseMapperFactory.newInstance(commonMappersFactory);

        checkingRequestMappersFactory =
                CheckingRequestMappersFactory.newInstance(commonExternalParametersProvider);

        fetchTransactionsRequestsFactory = FetchTransactionsRequestMappersFactory.newInstance();
        fetchTransactionsResponseMapperFactory =
                FetchTransactionsResponseMapperFactory.newInstance(
                        fetchAccountsResponseMapperFactory, commonMappersFactory);
    }

    public static MappersController newInstance(
            CommonExternalParametersProvider commonExternalParametersProvider,
            CredentialsRequest request) {
        return new MappersController(commonExternalParametersProvider, request);
    }

    public AuthenticationRequestMapper authenticationRequestMapper() {
        if (authenticationRequestMapper == null) {
            authenticationRequestMapper =
                    authenticationRequestMapperFactory.authenticationRequestMapper();
        }
        return authenticationRequestMapper;
    }

    public FetchAccountsRequestMapper fetchAccountsRequestMapper() {
        if (fetchAccountsRequestMapper == null) {
            this.fetchAccountsRequestMapper =
                    checkingRequestMappersFactory.fetchAccountsRequestMapper();
        }
        return fetchAccountsRequestMapper;
    }

    public FetchAccountsResponseMapper fetchAccountsResponseMapper() {
        if (fetchAccountsResponseMapper == null) {
            this.fetchAccountsResponseMapper =
                    fetchAccountsResponseMapperFactory.fetchAccountsResponseMapper();
        }
        return fetchAccountsResponseMapper;
    }

    public ThirdPartyAppAuthenticationPayloadMapper thirdPartyAppAuthenticationPayloadMapper() {
        if (thirdPartyAppAuthenticationPayloadMapper == null) {
            thirdPartyAppAuthenticationPayloadMapper =
                    authenticationResponseMappersFactory.thirdPartyAppAuthenticationPayloadMapper();
        }
        return thirdPartyAppAuthenticationPayloadMapper;
    }

    public FetchTransactionsRequestMapper fetchTransactionsRequestMapper() {
        if (fetchTransactionsRequestMapper == null) {
            fetchTransactionsRequestMapper =
                    fetchTransactionsRequestsFactory.fetchTransactionsRequestMapper();
        }
        return fetchTransactionsRequestMapper;
    }

    public FetchTransactionsResponseMapper fetchTransactionsResponseMapper() {
        if (fetchTransactionsResponseMapper == null) {
            fetchTransactionsResponseMapper =
                    fetchTransactionsResponseMapperFactory.fetchTransactionsResponseMapper();
        }
        return fetchTransactionsResponseMapper;
    }
}
