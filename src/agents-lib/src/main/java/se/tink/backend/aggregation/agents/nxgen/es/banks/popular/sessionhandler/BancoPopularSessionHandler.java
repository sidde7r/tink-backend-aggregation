package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.sessionhandler.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BancoPopularSessionHandler implements SessionHandler {

    private final BancoPopularApiClient bankClient;

    public BancoPopularSessionHandler(BancoPopularApiClient bankClient) {

        this.bankClient = bankClient;
    }
    @Override
    public void logout() {
        // no such method in the api
    }

    @Override
    public void keepAlive() throws SessionException {
        KeepAliveResponse response = bankClient.keepAlive();

        if (response.isOk()) {
            return;
        }

        throw SessionError.SESSION_EXPIRED.exception();
    }
}
