package se.tink.backend.aggregation.agents.bankid;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.SignicatBankIdHandler;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.SignicatBankIdStatus;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

public class CredentialsSignicatBankIdAuthenticationHandler implements SignicatBankIdHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Credentials credentials;
    private final SupplementalInformationController supplementalInformationController;

    public CredentialsSignicatBankIdAuthenticationHandler(
            Credentials credentials,
            SupplementalInformationController supplementalInformationController) {
        this.credentials = credentials;
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public void onUpdateStatus(SignicatBankIdStatus status) {
        switch (status) {
            case AUTHENTICATED:
                credentials.setStatus(CredentialsStatus.UPDATING);
                break;
            case AUTHENTICATION_ERROR:
                credentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
                break;
            case AWAITING_BANKID_AUTHENTICATION:
                supplementalInformationController.openMobileBankIdAsync(null);
                break;
            default:
                logger.error("Unknown authentication status: " + status);
                credentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
                break;
        }
    }
}
