package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionsEntity implements TransactionKeyPaginatorResponse<String> {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<BookedEntity> booked;

    public LinksEntity getLinks() {
        return links;
    }

    public List<BookedEntity> getBooked() {
        return booked;
    }

    private Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    @Override
    public String nextKey() {
        String url = this.getLinks().getPrevious().getHref().split("\\?")[1];
        return url;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {

        return booked.stream()
                .map(
                        movement -> {
                            // Format: 2019-01-01T02:30:00.000+00:00
                            DateTime dt = new DateTime(movement.getBookingDate());
                            Date d = dt.toDate();

                            return Transaction.builder()
                                    .setAmount(
                                            new Amount(
                                                    movement.getTransactionAmount().getCurrency(),
                                                    new Double(
                                                            movement.getTransactionAmount()
                                                                    .getAmount())))
                                    .setDescription(movement.getRemittanceInformationUnstructured())
                                    .setDate(d)
                                    .build();
                        })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        /*
            TODO: Not returning false causes an infinite loop in pagination as we always
            get the same key for the next page. We will fix it in production.
        */
        // return Optional.of(this.getLinks().getPrevious() != null);
        return Optional.of(false);
    }
}
