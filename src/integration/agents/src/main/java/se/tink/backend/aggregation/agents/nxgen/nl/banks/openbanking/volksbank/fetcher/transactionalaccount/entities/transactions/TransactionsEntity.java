package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionsEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<BookedEntity> booked;

    public LinksEntity getLinks() {
        return links;
    }

    public List<BookedEntity> getBooked() {
        return booked;
    }

    public List<Transaction> toTinkTransactions(Date limitDate) {
        return booked.stream()
                .filter(bookedEntity -> Objects.nonNull(bookedEntity.getEntryReference()))
                .filter(
                        bookedEntity ->
                                !VolksbankUtils.isEntryReferenceFromAfterDate(
                                        bookedEntity.getEntryReference(), limitDate))
                .map(
                        movement ->
                                Transaction.builder()
                                        .setAmount(createAmount(movement))
                                        .setDescription(createDescription(movement))
                                        .setDate(createDate(movement))
                                        .setPayload(
                                                TransactionPayloadTypes.TRANSFER_ACCOUNT_EXTERNAL,
                                                getCounterPartyAccount(movement))
                                        .setPayload(
                                                TransactionPayloadTypes
                                                        .TRANSFER_ACCOUNT_NAME_EXTERNAL,
                                                getCounterPartyName(movement))
                                        .setPayload(
                                                TransactionPayloadTypes.MESSAGE,
                                                movement.getRemittanceInformationUnstructured())
                                        .build())
                .collect(Collectors.toList());
    }

    private static ExactCurrencyAmount createAmount(final BookedEntity movement) {
        return ExactCurrencyAmount.of(
                new BigDecimal(movement.getTransactionAmount().getAmount()),
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

    private static String getCounterPartyAccount(final BookedEntity movement) {
        return Stream.of(
                        movement.getCreditorAccount().getIban(),
                        movement.getDebtorAccount().getIban())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse("");
    }

    private static String getCounterPartyName(final BookedEntity movement) {
        return Stream.of(movement.getCreditorName(), movement.getDebtorName())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse("");
    }
}
