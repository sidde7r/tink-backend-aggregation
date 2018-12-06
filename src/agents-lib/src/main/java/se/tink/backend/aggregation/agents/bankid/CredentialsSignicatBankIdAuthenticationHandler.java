package se.tink.backend.aggregation.agents.bankid;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.SignicatBankIdHandler;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.SignicatBankIdStatus;

public class CredentialsSignicatBankIdAuthenticationHandler implements SignicatBankIdHandler {
    private static final AggregationLogger log = new AggregationLogger(CredentialsSignicatBankIdAuthenticationHandler.class);

    private final Credentials credentials;
    private final AgentContext context;

    public CredentialsSignicatBankIdAuthenticationHandler(Credentials credentials, AgentContext context) {
        this.credentials = credentials;
        this.context = context;
    }

    @Override
    public void onUpdateStatus(SignicatBankIdStatus status, String statusPayload, String nationalId) {
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

            context.requestSupplementalInformation(credentials, false);
            break;
        default:
            log.error("Unknown authentication status: " + status);
            credentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            break;
        }
    }
}
