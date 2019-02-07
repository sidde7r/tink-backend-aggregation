package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class GroupedMovementsEntity {
    private boolean moreElements;
    private List<PeriodMovementEntity> periodMovementModelList;

    public boolean hasMoreElements() {
        return moreElements;
    }

    public List<PeriodMovementEntity> getPeriodMovementModelList() {
        return periodMovementModelList;
    }

    public Collection<CreditCardTransaction> toTinkTransactions(CreditCardAccount creditCardAccount) {
        List<CreditCardTransaction> transactions = new ArrayList<>();

        Optional.ofNullable(periodMovementModelList).orElse(Collections.emptyList())
                .forEach(periodMovementEntity ->
                        transactions.addAll(getPeriodTransactions(periodMovementEntity, creditCardAccount)));

        return transactions;
    }

    private List<CreditCardTransaction> getPeriodTransactions(PeriodMovementEntity periodMovementEntity,
            CreditCardAccount creditCardAccount) {
        MovementWrapperListEntity movementWrapperList = periodMovementEntity.getGenericMovementWrapperList();

        if (movementWrapperList == null) {
            return Collections.emptyList();
        }

        return Optional.ofNullable(movementWrapperList.getMovements()).orElse(Collections.emptyList())
                .stream()
                .filter(movementEntity -> Objects.nonNull(movementEntity.getCardMovement()))
                .map(movementEntity -> movementEntity.getCardMovement().toTinkTransaction(creditCardAccount))
                .collect(Collectors.toList());
    }
}
