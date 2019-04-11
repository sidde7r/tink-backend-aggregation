package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionListEntity {
    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    @JsonProperty("_links")
    private List<TransactionLinksEntity> links;

    public List<Transaction> toTinkTransactions() {
        List<Transaction> result = new ArrayList<>();
        result.addAll(
                booked.stream().map(BookedEntity::toTinkTransaction).collect(Collectors.toList()));
        result.addAll(
                pending.stream()
                        .map(PendingEntity::toTinkTransaction)
                        .collect(Collectors.toList()));
        return result;
    }

    public String getTotalPages() {
        Matcher m = RaiffeisenConstants.REGEX.PAGE.matcher(links.get(0).getLastPage());

        if (m.find()) {
            return m.group();
        }

        return null;
    }

    public List<BookedEntity> getBooked() {
        return booked;
    }

    public List<PendingEntity> getPending() {
        return pending;
    }
}
