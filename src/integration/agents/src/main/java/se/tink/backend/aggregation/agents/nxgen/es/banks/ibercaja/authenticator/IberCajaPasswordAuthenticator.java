package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IberCajaPasswordAuthenticator implements PasswordAuthenticator {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(IberCajaPasswordAuthenticator.class);

    private final IberCajaApiClient bankClient;
    private final IberCajaSessionStorage iberCajaSessionStorage;

    public IberCajaPasswordAuthenticator(
            IberCajaApiClient bankClient, IberCajaSessionStorage iberCajaSessionStorage) {

        this.bankClient = bankClient;
        this.iberCajaSessionStorage = iberCajaSessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        // Construct session request from username and password
        SessionResponse sessionResponse =
                bankClient.initializeSession(
                        new SessionRequest(
                                username,
                                password,
                                IberCajaConstants.DefaultRequestParams.CARD,
                                IberCajaConstants.DefaultRequestParams.LAST_ACCESS));

        LoginResponse loginResponse =
                bankClient.login(
                        new LoginRequest(sessionResponse.getNici()),
                        sessionResponse.getTicket(),
                        sessionResponse.getUser());

        iberCajaSessionStorage.saveLoginResponse(
                loginResponse.getAccessToken(), loginResponse.getRefreshToken());
        iberCajaSessionStorage.saveUsername(username);
        iberCajaSessionStorage.saveTicket(sessionResponse.getTicket());
        iberCajaSessionStorage.saveFullName(sessionResponse.getName());

        if (Strings.isNullOrEmpty(sessionResponse.getNif())) {
            // Logging whole session response in case NIF value isn't present. Expecting to find
            // a field called NIE or something else in that case. But we'll have to log the whole
            // session response to find out.
            LOGGER.infoExtraLong(
                    SerializationUtils.serializeToString(sessionResponse),
                    IberCajaConstants.Log.NIF_NOT_PRESENT);
        } else {
            iberCajaSessionStorage.saveDocumentNumber(sessionResponse.getNif());
        }
    }
}
