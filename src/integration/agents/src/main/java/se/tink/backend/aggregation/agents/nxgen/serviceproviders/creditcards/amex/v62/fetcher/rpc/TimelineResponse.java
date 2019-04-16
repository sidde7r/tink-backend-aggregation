package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.SubcardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TimelineEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TimelineResponse {
    private TimelineEntity timeline;

    public TimelineEntity getTimeline() {
        return timeline;
    }

    @JsonIgnore
    public List<CreditCardAccount> getAccounts(
            final AmericanExpressV62Configuration configuration) {
        return timeline.getCreditCardAccounts(configuration);
    }

    @JsonIgnore
    public String getSuppIndexForAccount(final CreditCardAccount account) {
        return timeline.getCardList().stream()
                .filter(
                        subCard ->
                                account.getAccountNumber()
                                        .equalsIgnoreCase(
                                                subCard.transformCardNameToAccountNumber()))
                .map(SubcardEntity::getSuppIndex)
                .findAny()
                .orElseThrow(NoSuchElementException::new);
    }

    @JsonIgnore
    public Set<Transaction> getPendingTransactions(
            final AmericanExpressV62Configuration configuration, final String suppIndex) {
        return timeline.getTransactions(configuration, suppIndex).stream()
                .filter(Transaction::isPending)
                .collect(Collectors.toSet());
    }
}
