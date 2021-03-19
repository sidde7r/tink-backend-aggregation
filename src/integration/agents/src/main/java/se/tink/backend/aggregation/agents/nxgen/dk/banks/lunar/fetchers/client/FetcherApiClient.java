package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.HeaderValues;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Headers;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.PathParams;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.QueryParams;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.QueryParamsValues;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Url;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc.InstrumentsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc.InvestmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc.PortfolioPerformanceResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.GoalDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.GoalsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.MembersResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class FetcherApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final LunarDataAccessorFactory accessorFactory;
    private final RandomValueGenerator randomValueGenerator;
    private final String languageCode;
    private LunarAuthData authData;

    public GoalsResponse fetchSavingGoals() {
        return get(Url.GOALS, GoalsResponse.class);
    }

    public TransactionsResponse fetchTransactions(String originGroupId, String timestampKey) {
        return get(
                Url.TRANSACTIONS
                        .queryParam(QueryParams.ORIGIN_GROUP_ID, originGroupId)
                        .queryParam(
                                QueryParams.PAGE_SIZE, String.valueOf(QueryParamsValues.PAGE_SIZE))
                        .queryParam(QueryParams.BEFORE_QUERY, timestampKey),
                TransactionsResponse.class);
    }

    public GoalDetailsResponse fetchGoalDetails(String goalId) {
        return get(
                Url.GOAL_DETAILS.parameter(PathParams.GOAL_ID, goalId), GoalDetailsResponse.class);
    }

    public CardsResponse fetchCardsByAccount(String accountId) {
        return get(
                Url.CARDS_BY_ACCOUNT.parameter(PathParams.ACCOUNT_ID, accountId),
                CardsResponse.class);
    }

    public MembersResponse fetchMembers(String accountId) {
        return get(Url.MEMBERS.parameter(PathParams.ACCOUNT_ID, accountId), MembersResponse.class);
    }

    public InvestmentsResponse fetchInvestments() {
        return get(Url.PORTFOLIO, InvestmentsResponse.class);
    }

    public PortfolioPerformanceResponse fetchPerformanceData() {
        return get(Url.PORTFOLIO_PERFORMANCE_DATA, PortfolioPerformanceResponse.class);
    }

    public InstrumentsResponse fetchInstruments() {
        return get(Url.INSTRUMENTS, InstrumentsResponse.class);
    }

    private <T> T get(URL url, Class<T> responseClass) {
        authData = getLunarPersistedData();
        try {
            return client.request(url)
                    .header(Headers.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                    .header(Headers.USER_AGENT, HeaderValues.USER_AGENT_VALUE)
                    .header(Headers.REGION, HeaderValues.DK_REGION)
                    .header(Headers.OS, HeaderValues.I_OS)
                    .header(Headers.DEVICE_MANUFACTURER, HeaderValues.DEVICE_MANUFACTURER)
                    .header(Headers.OS_VERSION, HeaderValues.OS_VERSION)
                    .header(Headers.LANGUAGE, languageCode)
                    .header(Headers.REQUEST_ID, randomValueGenerator.getUUID().toString())
                    .header(Headers.DEVICE_ID, authData.getDeviceId())
                    .header(Headers.AUTHORIZATION, authData.getAccessToken())
                    .header(Headers.ORIGIN, HeaderValues.APP_ORIGIN)
                    .header(Headers.APP_VERSION, LunarConstants.APP_VERSION)
                    .header(Headers.ACCEPT_ENCODING, HeaderValues.ENCODING)
                    .acceptLanguage(HeaderValues.DA_LANGUAGE_ACCEPT)
                    .accept(MediaType.WILDCARD_TYPE)
                    .get(responseClass);
        } catch (HttpResponseException e) {
            throwBankSideErrorOrOriginal(e);
            throw e;
        }
    }

    private void throwBankSideErrorOrOriginal(HttpResponseException e) {
        switch (e.getResponse().getStatus()) {
            case 500:
                throw BankServiceError.BANK_SIDE_FAILURE.exception("Http status: " + 500);
            case 502:
            case 503:
            case 504:
                throw BankServiceError.NO_BANK_SERVICE.exception(
                        "Http status: " + e.getResponse().getStatus());
            default:
                throw e;
        }
    }

    private LunarAuthData getLunarPersistedData() {
        if (authData == null) {
            authData =
                    accessorFactory
                            .createAuthDataAccessor(
                                    new PersistentStorageService(persistentStorage)
                                            .readFromAgentPersistentStorage())
                            .get();
        }
        return authData;
    }
}
