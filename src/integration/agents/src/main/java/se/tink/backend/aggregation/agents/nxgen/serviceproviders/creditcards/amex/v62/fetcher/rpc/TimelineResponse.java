package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TimelineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class TimelineResponse {
    private TimelineEntity timeline;

    public TimelineEntity getTimeline() {
        return timeline;
    }

    @JsonIgnore
    public boolean isValidResponse() {
        return !Objects.isNull(timeline) && !Objects.isNull(timeline.getTimelineItems());
    }

    @JsonIgnore
    public List<CreditCardAccount> getAccounts(
            final AmericanExpressV62Configuration configuration) {
        return timeline.getCreditCardAccounts(configuration);
    }

    @JsonIgnore
    public Set<TransactionEntity> getPendingTransactions(final String suppIndex) {
        return timeline.getTransactions(suppIndex);
    }
}
