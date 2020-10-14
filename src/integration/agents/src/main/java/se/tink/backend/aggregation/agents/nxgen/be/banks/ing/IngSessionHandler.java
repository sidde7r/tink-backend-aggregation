package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.IndividualsResponseEntity;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class IngSessionHandler implements SessionHandler {

    private final IngProxyApiClient ingProxyApiClient;

    public IngSessionHandler(IngProxyApiClient ingProxyApiClient) {
        this.ingProxyApiClient = ingProxyApiClient;
    }

    @Override
    public void logout() {
        // no logout
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            IndividualsResponseEntity individuals = ingProxyApiClient.getIndividuals();
            if (individuals.getIndividual() == null
                    || StringUtils.isEmpty(individuals.getIndividual().getId())) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }
}
