package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentAccountEntity extends AbstractAccountEntity {
    private boolean defaultPaymentAccount;

    public boolean isDefaultPaymentAccount() {
        return defaultPaymentAccount;
    }
}
