package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class IberCajaSessionHandler implements SessionHandler {

    private IberCajaApiClient bankClient;

    public IberCajaSessionHandler(IberCajaApiClient bankClient) {

        this.bankClient = bankClient;
    }

    @Override
    public void logout() {

    }

    @Override
    public void keepAlive() throws SessionException {
        if (!bankClient.isAlive()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
