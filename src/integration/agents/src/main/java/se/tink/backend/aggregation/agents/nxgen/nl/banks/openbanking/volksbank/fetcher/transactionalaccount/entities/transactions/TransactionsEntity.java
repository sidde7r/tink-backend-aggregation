package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.utils.VolksbankUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonObject
public class TransactionsEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<BookedEntity> booked;

    @JsonIgnore
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
                                        .setDate(parseDate(movement.getEntryReference()))
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

    public String getNextLink() {
        return links != null ? links.getNext().getHref().split("\\?")[1] : null;
    }

    private static ExactCurrencyAmount createAmount(final BookedEntity movement) {
        return ExactCurrencyAmount.of(
                new BigDecimal(movement.getTransactionAmount().getAmount()),
                movement.getTransactionAmount().getCurrency());
    }

    @SneakyThrows(ParseException.class)
    private static Date parseDate(final String entryReference) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        final String dateString =
                Optional.ofNullable(entryReference)
                        .map(s -> s.split("-")[0])
                        .orElseThrow(IllegalStateException::new);
        return formatter.parse(dateString);
    }

    private static String createDescription(final BookedEntity movement) {
        return Stream.of(
                        movement.getDebtorName(),
                        movement.getCreditorName(),
                        getStructuredDescription(movement.getRemittanceInformationUnstructured()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Couldn't find description"));
    }

    private static String getCounterPartyAccount(final BookedEntity movement) {
        return Stream.of(
                        movement.getCreditorAccount().getIban(),
                        movement.getDebtorAccount().getIban())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    private static String getCounterPartyName(final BookedEntity movement) {
        return Stream.of(movement.getCreditorName(), movement.getDebtorName())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    private static String getStructuredDescription(String unStructuredDescription) {
        return String.join(" ", unStructuredDescription.split("\\s+"));
    }
}
