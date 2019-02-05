package se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat;

public interface SignicatBankIdHandler {
    /**
     * Callback on status update from the authentication process
     *
     * @param status Authentication status
     * @param statusPayload Authentication status message or autostart-token (for AWAITING_BANKID_AUTHENTICATION)
     * @param nationalId the authenticated national id
     */
    void onUpdateStatus(SignicatBankIdStatus status, String statusPayload, String nationalId);
}
