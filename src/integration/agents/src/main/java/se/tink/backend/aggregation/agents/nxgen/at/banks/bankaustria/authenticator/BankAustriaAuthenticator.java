package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.authenticator;

import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.OtmlResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml.OtmlResponseConverter;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.agents.rpc.Credentials;

import java.util.Optional;

public class BankAustriaAuthenticator implements PasswordAuthenticator {
    private final BankAustriaApiClient apiClient;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final BankAustriaSessionStorage bankAustriaSessionStorage;
    private OtmlResponseConverter otmlResponseConverter;

    public BankAustriaAuthenticator(BankAustriaApiClient apiClient, Credentials credentials, PersistentStorage persistentStorage, BankAustriaSessionStorage bankAustriaSessionStorage, OtmlResponseConverter otmlResponseConverter) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.bankAustriaSessionStorage = bankAustriaSessionStorage;
        this.otmlResponseConverter = otmlResponseConverter;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        bankAustriaSessionStorage.setXOtmlManifest(apiClient.getMD5OfUpdatePage());

        OtmlResponse response = apiClient.login(removeDotsNotUsedByApp(username), password);

        if (successful(response)) {
            return;
        }

        if (wrongCredentials(response)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        throw new IllegalStateException("Could not authenticate");
    }

    private String removeDotsNotUsedByApp(String username) {
        return username.replace(".", "");
    }

    private boolean successful(OtmlResponse response) {
        return resultNodeContainsOk(response);
    }

    private boolean resultNodeContainsOk(OtmlResponse response) {
        return otmlResponseConverter.getAccountNodeExists(response.getDataSources());
    }

    public boolean wrongCredentials(OtmlResponse response) {
        if (userIdErrorSetInParams(response)
                || passwordMissingOrInvalidFormatSetInParams(response)) {
            return true;
        }

        return resultNodeContainsKo(response);
    }

    private boolean resultNodeContainsKo(OtmlResponse response) {
        Optional<Node> result = otmlResponseConverter.getResultNode(response.getDataSources());
        return result.filter(node -> BankAustriaConstants.NOT_OK.equals(otmlResponseConverter.getValue(node))).isPresent();
    }

    private boolean passwordMissingOrInvalidFormatSetInParams(OtmlResponse response) {
        return response.getParams().getPasswordError() != null;
    }

    private boolean userIdErrorSetInParams(OtmlResponse response) {
        return response.getParams().getUserIdError() != null;
    }
}
