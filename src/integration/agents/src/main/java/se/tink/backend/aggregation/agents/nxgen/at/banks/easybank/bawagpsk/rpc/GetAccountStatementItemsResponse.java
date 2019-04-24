package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountStatementItem;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.GetAccountStatementItemsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.OK;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

public final class GetAccountStatementItemsResponse {
    private Envelope envelope;

    public GetAccountStatementItemsResponse(Envelope envelope) {
        this.envelope = envelope;
    }

    /**
     * In the bank's nomenclature, the "ValueDate" is the date of requesting the money to be
     * transferred, whereas the "BookingDate" is the date when the money hits the account. That is,
     * ValueDate <= BookingDate.
     */
    private Optional<Date> dateOfRequestingTheTransfer(final AccountStatementItem item) {
        return Optional.ofNullable(item)
                .map(AccountStatementItem::getValueDate)
                .map(date -> Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private Optional<? extends Transaction> buildTransaction(final AccountStatementItem item) {
        final Optional<String> currency =
                Optional.ofNullable(item)
                        .map(AccountStatementItem::getAmountEntity)
                        .map(AmountEntity::getCurrency);
        final Optional<Double> balance =
                Optional.ofNullable(item)
                        .map(AccountStatementItem::getAmountEntity)
                        .map(AmountEntity::getAmount);
        final Optional<Date> date = dateOfRequestingTheTransfer(item);
        final String description =
                String.join(
                        " ",
                        Optional.ofNullable(item.getTextLines()).orElseGet(Collections::emptyList));
        if (currency.isPresent() && balance.isPresent() && date.isPresent()) {
            return Optional.of(
                    Transaction.builder()
                            .setAmount(new Amount(currency.get(), balance.get()))
                            .setDate(date.get())
                            .setDescription(description)
                            .build());
        }
        return Optional.empty();
    }

    public Collection<? extends Transaction> getTransactions() {
        return Optional.ofNullable(envelope)
                .map(Envelope::getBody)
                .map(Body::getGetAccountStatementItemsResponseEntity)
                .map(GetAccountStatementItemsResponseEntity::getOk)
                .map(OK::getAccountStatementItemList)
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(this::buildTransaction)
                .flatMap(t -> t.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toSet());
    }
}
