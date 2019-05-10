package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class FetchCreditCardsResponse {
    @JsonIgnore
    public List<CreditCardAccount> toTinkCards() {
        return Collections.emptyList();
    }
}
