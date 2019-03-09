package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.rpc;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.entity.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class CreditResponse {
    private Data data;

    public Collection<CreditCardAccount> getCreditCardAccounts() {
        return data.getCreditCardAccounts();
    }
}
