package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.sessionhandler;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class OpBankSessionHandler implements SessionHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OpBankApiClient bankClient;

    public OpBankSessionHandler(OpBankApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public void logout() {
        OpBankResponseEntity response = bankClient.logout();
        if (!response.isSuccess()) {
            logger.warn("Failed to logout with status: " + response.getStatus());
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            OpBankResponseEntity opBankResponseEntity = bankClient.refreshSession();
            if (!opBankResponseEntity.isSuccess()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }
}
