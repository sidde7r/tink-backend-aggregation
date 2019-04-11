package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountantBalanceEntity extends Amount {

    private String date;

    public AccountantBalanceEntity(@JsonProperty("solde") double balance) {
        super(LaBanquePostaleConstants.CURRENCY, balance);
    }
}
