package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.sessionhandler;

import java.util.Optional;
import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.OtmlResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml.OtmlResponseConverter;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BankAustriaSessionHandler implements SessionHandler {
    private BankAustriaApiClient apiClient;
    private OtmlResponseConverter otmlResponseConverter;

    public BankAustriaSessionHandler(
            BankAustriaApiClient apiClient, OtmlResponseConverter otmlResponseConverter) {
        this.apiClient = apiClient;
        this.otmlResponseConverter = otmlResponseConverter;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        OtmlResponse accountsFromSettings = apiClient.getAccountsFromSettings();
        Optional<Node> resultNode =
                otmlResponseConverter.getResultNode(accountsFromSettings.getDataSources());
        // Assumption, this happens when tested, if session(cookie) alive ok is returned in the
        // result node
        if (resultNode.isPresent()) {
            if (BankAustriaConstants.OK.equals(otmlResponseConverter.getValue(resultNode.get()))) {
                return;
            }
        }
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
