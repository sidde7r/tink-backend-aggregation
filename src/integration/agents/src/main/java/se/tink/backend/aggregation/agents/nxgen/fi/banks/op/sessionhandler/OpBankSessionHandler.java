package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class OpBankSessionHandler implements SessionHandler {
    private static final AggregationLogger LOG = new AggregationLogger(OpBankSessionHandler.class);
    private OpBankApiClient bankClient;

    public OpBankSessionHandler(OpBankApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public void logout() {
        OpBankResponseEntity response = bankClient.logout();
        if (!response.isSuccess()) {
            LOG.warn("Failed to logout with status: " + response.getStatus());
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
