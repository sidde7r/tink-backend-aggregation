package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities.CreditCardAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class FetchCreditCardsResponse extends ArrayList<CreditCardAccountEntity> {
    @JsonIgnore
    public Collection<CreditCardAccount> getTinkCreditCardAccounts() {
        return stream()
                .map(CreditCardAccountEntity::toTinkCreditCardAccount)
                .collect(Collectors.toList());
    }
}
