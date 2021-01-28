package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.HeaderValues;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Headers;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Url;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class LunarApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final RandomValueGenerator randomValueGenerator;
    private LunarAuthData authData;

    public AccountsResponse fetchAccounts() {
        authData = getLunarPersistedData();

        return client.request(Url.ACCOUNTS_VIEW)
                .header(Headers.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .header(Headers.USER_AGENT, HeaderValues.USER_AGENT_VALUE)
                .header(Headers.REGION, HeaderValues.DK_REGION)
                .header(Headers.OS, HeaderValues.I_OS)
                .header(Headers.DEVICE_MANUFACTURER, HeaderValues.DEVICE_MANUFACTURER)
                .header(Headers.OS_VERSION, HeaderValues.OS_VERSION)
                .header(Headers.LANGUAGE, LunarConstants.DA_LANGUAGE)
                .header(Headers.REQUEST_ID, randomValueGenerator.getUUID().toString())
                .header(Headers.DEVICE_ID, authData.getDeviceId())
                .header(Headers.AUTHORIZATION, authData.getAccessToken())
                .header(Headers.ACCEPT_LANGUAGE, HeaderValues.DA_LANGUAGE_ACCEPT)
                .header(Headers.ORIGIN, HeaderValues.APP_ORIGIN)
                .header(Headers.APP_VERSION, LunarConstants.APP_VERSION)
                .header(Headers.ACCEPT, HeaderValues.ACCEPT_ALL)
                .header(Headers.ACCEPT_ENCODING, HeaderValues.ENCODING)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(AccountsResponse.class);
    }

    private LunarAuthData getLunarPersistedData() {
        if (authData == null) {
            authData =
                    new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance())
                            .createAuthDataAccessor(
                                    new PersistentStorageService(persistentStorage)
                                            .readFromAgentPersistentStorage())
                            .get();
        }
        return authData;
    }
}
