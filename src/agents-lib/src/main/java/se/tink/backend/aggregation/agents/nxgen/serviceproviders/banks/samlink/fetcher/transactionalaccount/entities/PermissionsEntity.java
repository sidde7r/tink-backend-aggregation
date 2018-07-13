package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PermissionsEntity {
    private Boolean ownTransferDebtor;
    private Boolean ownTransferCreditor;
    private Boolean paymentDebtor;

    public Boolean getOwnTransferDebtor() {
        return ownTransferDebtor;
    }

    public Boolean getOwnTransferCreditor() {
        return ownTransferCreditor;
    }

    public Boolean getPaymentDebtor() {
        return paymentDebtor;
    }
}
