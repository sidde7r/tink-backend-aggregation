package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.entities.SecurityParamsRequestBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.utils.SparebankenVestAuthUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;

public class SparebankenVestAutoAuthenticator implements AutoAuthenticator {
    private final SparebankenVestApiClient apiClient;
    private final EncapClient encapClient;

    private SparebankenVestAutoAuthenticator(SparebankenVestApiClient apiClient, EncapClient encapClient) {
        this.apiClient = apiClient;
        this.encapClient = encapClient;
    }

    public static SparebankenVestAutoAuthenticator create(SparebankenVestApiClient apiClient,
            EncapClient encapClient) {
        return new SparebankenVestAutoAuthenticator(apiClient, encapClient);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        apiClient.initLogin();

        String securityToken = encapClient.authenticateUser();
        String encapHardwareId = encapClient.getEncapHardwareId();

        String htmlResponseString = apiClient.authenticate(securityToken, encapHardwareId);

        SecurityParamsRequestBody securityParamsRequestBody =
                SparebankenVestAuthUtils.createSecurityParamsRequestBody(htmlResponseString);
        htmlResponseString = apiClient.postSecurityParamsAuthentication(securityParamsRequestBody);

        securityParamsRequestBody = SparebankenVestAuthUtils.createSecurityParamsRequestBody(htmlResponseString);
        apiClient.finalizeLogin(securityParamsRequestBody);
    }
}
