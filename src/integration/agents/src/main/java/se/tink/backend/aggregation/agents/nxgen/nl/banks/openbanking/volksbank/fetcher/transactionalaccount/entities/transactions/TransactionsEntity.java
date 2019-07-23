package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
        return this.getLinks().getNext().getHref().split("\\?")[1];
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {

        return booked.stream()
                .map(
                        movement ->
                                Transaction.builder()
                                        .setAmount(createAmount(movement))
                                        .setDescription(createDescription(movement))
                                        .setDate(createDate(movement))
                                        .build())
                .collect(Collectors.toList());
    }

    private static ExactCurrencyAmount createAmount(final BookedEntity movement) {
        return ExactCurrencyAmount.of(
                new Double(movement.getTransactionAmount().getAmount()),
                movement.getTransactionAmount().getCurrency());
    }

    private static Date createDate(final BookedEntity movement) {
        // Observed values:
        // entryReference: "20181003-80299889"
        // bookingDate: "2018-10-02"
        // valueDate: "2018-10-02"
        // The date shown in the bank app seems to be based on entryReference

        final String dateString =
                Optional.ofNullable(movement)
                        .map(BookedEntity::getEntryReference)
                        .map(s -> s.split("-")[0])
                        .map(s -> s.substring(0, 6) + "-" + s.substring(6))
                        .map(s -> s.substring(0, 4) + "-" + s.substring(4))
                        .orElseThrow(IllegalStateException::new);

        return new DateTime(dateString).toDate();
    }

    private static String createDescription(final BookedEntity movement) {
        if (Objects.nonNull(movement.getDebtorName())) {
            return movement.getDebtorName();
        } else if (Objects.nonNull(movement.getCreditorName())) {
            return movement.getCreditorName();
        } else if (Objects.nonNull(movement.getRemittanceInformationUnstructured())) {
            final String unstructured = movement.getRemittanceInformationUnstructured();
            final String[] words = unstructured.split("\\s+");
            return String.join(" ", words);
        }
        throw new IllegalStateException("Couldn't find description");
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(this.getLinks() != null && this.getLinks().getNext() != null);
    }
}
