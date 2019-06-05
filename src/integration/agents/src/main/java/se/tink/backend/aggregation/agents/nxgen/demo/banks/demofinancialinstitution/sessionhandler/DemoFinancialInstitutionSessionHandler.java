package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class DemoFinancialInstitutionSessionHandler implements SessionHandler {
    private DemoFinancialInstitutionApiClient client;

    public DemoFinancialInstitutionSessionHandler(DemoFinancialInstitutionApiClient client) {
        this.client = client;
    }

    @Override
    public void logout() {
        // TODO: Logout
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
