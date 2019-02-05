package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RightsEntity {
    private boolean accessPermission;
    private boolean paymentFrom;
    private boolean transferFrom;
    private boolean transferTo;
    private boolean createDisposalRole;
    private boolean createPaymentAgreement;

    public boolean isAccessPermission() {
        return accessPermission;
    }

    public boolean isPaymentFrom() {
        return paymentFrom;
    }

    public boolean isTransferFrom() {
        return transferFrom;
    }

    public boolean isTransferTo() {
        return transferTo;
    }

    public boolean isCreateDisposalRole() {
        return createDisposalRole;
    }

    public boolean isCreatePaymentAgreement() {
        return createPaymentAgreement;
    }
}
