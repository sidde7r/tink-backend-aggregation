package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.authentication.rpc.BanqueTranatlantiqueLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BanqueTransatlantiqueApiClient extends EuroInformationApiClient {
    public BanqueTransatlantiqueApiClient(TinkHttpClient client,
            SessionStorage sessionStorage,
            EuroInformationConfiguration config) {
        super(client, sessionStorage, config);
    }

    @Override
    public LoginResponse logon(String username, String password) {
        return buildRequestHeaders(config.getLoginSubpage())
                .body(new BanqueTranatlantiqueLoginRequest(
                        username,
                        password,
                        config.getAppVersionKey(),
                        config.getAppVersion(),
                        config.getTarget()))
                .post(LoginResponse.class);
    }
}
