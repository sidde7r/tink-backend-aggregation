package se.tink.backend.aggregation.agents.bankid;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.SignicatBankIdHandler;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.SignicatBankIdStatus;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

public class CredentialsSignicatBankIdAuthenticationHandler implements SignicatBankIdHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Credentials credentials;
    private final SupplementalRequester supplementalRequester;

    public CredentialsSignicatBankIdAuthenticationHandler(
            Credentials credentials, SupplementalRequester context) {
        this.credentials = credentials;
        this.supplementalRequester = context;
    }

    @Override
    public void onUpdateStatus(
            SignicatBankIdStatus status, String statusPayload, String nationalId) {
        switch (status) {
            case AUTHENTICATED:
                credentials.setStatus(CredentialsStatus.UPDATING);
                break;
            case AUTHENTICATION_ERROR:
                credentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
                break;
            case AWAITING_BANKID_AUTHENTICATION:
                credentials.setSupplementalInformation(null);
                credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

                supplementalRequester.requestSupplementalInformation(credentials, false);
                break;
            default:
                logger.error("Unknown authentication status: " + status);
                credentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
                break;
        }
    }
}
