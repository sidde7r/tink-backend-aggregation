package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountantBalanceEntity extends ExactCurrencyAmount {

    private String date;

    public AccountantBalanceEntity(@JsonProperty("solde") double balance) {
        super(BigDecimal.valueOf(balance), LaBanquePostaleConstants.CURRENCY);
    }
}
