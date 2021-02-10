package se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat;

public interface SignicatBankIdHandler {
    /**
     * Callback on status update from the authentication process
     *
     * @param status Authentication status
     */
    void onUpdateStatus(SignicatBankIdStatus status);
}
