package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentAccountsEntity {
    private List<PaymentAccountEntity> paymentAccounts;

    public List<PaymentAccountEntity> getPaymentAccounts() {
        return paymentAccounts;
    }
}
