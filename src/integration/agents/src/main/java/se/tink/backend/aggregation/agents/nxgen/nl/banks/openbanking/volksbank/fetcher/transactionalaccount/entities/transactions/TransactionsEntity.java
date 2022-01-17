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
                        bookedEntity ->
                                Transaction.builder()
                                        .setAmount(createAmount(bookedEntity))
                                        .setDescription(createDescription(bookedEntity))
                                        .setDate(parseDate(bookedEntity.getEntryReference()))
                                        .setPayload(
                                                TransactionPayloadTypes.TRANSFER_ACCOUNT_EXTERNAL,
                                                getCounterPartyAccount(bookedEntity))
                                        .setPayload(
                                                TransactionPayloadTypes
                                                        .TRANSFER_ACCOUNT_NAME_EXTERNAL,
                                                getCounterPartyName(bookedEntity))
                                        .setPayload(
                                                TransactionPayloadTypes.MESSAGE,
                                                bookedEntity.getRemittanceInformationUnstructured())
                                        .build())
                .collect(Collectors.toList());
    }

    public String getNextLink() {
        return links != null && links.getNext() != null
                ? links.getNext().getHref().split("\\?")[1]
                : null;
    }

    private static ExactCurrencyAmount createAmount(final BookedEntity bookedEntity) {
        return ExactCurrencyAmount.of(
                new BigDecimal(bookedEntity.getTransactionAmount().getAmount()),
                bookedEntity.getTransactionAmount().getCurrency());
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

    private static String createDescription(final BookedEntity bookedEntity) {
        return Stream.of(
                        bookedEntity.getDebtorName(),
                        bookedEntity.getCreditorName(),
                        getStructuredDescription(
                                bookedEntity.getRemittanceInformationUnstructured()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Couldn't find description"));
    }

    private static String getCounterPartyAccount(final BookedEntity bookedEntity) {

        String creditorAccount =
                Optional.ofNullable(bookedEntity.getCreditorAccount())
                        .map(CreditorAccountEntity::getIban)
                        .filter(iban -> !iban.isEmpty())
                        .map(Optional::of)
                        .orElseGet(
                                () ->
                                        Optional.ofNullable(bookedEntity.getCreditorAccount())
                                                .map(CreditorAccountEntity::getBban))
                        .orElse(StringUtils.EMPTY);

        String debtorAccount =
                Optional.ofNullable(bookedEntity.getDebtorAccount())
                        .map(DebtorAccountEntity::getIban)
                        .filter(iban -> !iban.isEmpty())
                        .map(Optional::of)
                        .orElseGet(
                                () ->
                                        Optional.ofNullable(bookedEntity.getDebtorAccount())
                                                .map(DebtorAccountEntity::getBban))
                        .orElse(StringUtils.EMPTY);

        return Stream.of(creditorAccount, debtorAccount)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    private static String getCounterPartyName(final BookedEntity bookedEntity) {
        return Stream.of(bookedEntity.getCreditorName(), bookedEntity.getDebtorName())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    private static String getStructuredDescription(String unStructuredDescription) {
        return Optional.ofNullable(unStructuredDescription)
                .map(description -> String.join(" ", description.split("\\s+")))
                .orElse(null);
    }
}
