package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConfirmTransferResponse {
    private List<TransferTransactionEntity> confirmedTransactions;
    private List<TransferTransactionEntity> rejectedTransactions;
    private AmountEntity confirmedTotalAmount;

    public boolean isTransferConfirmed(String idToConfirm) {
        if (Strings.isNullOrEmpty(idToConfirm)) {
            return false;
        }

        return Optional.ofNullable(confirmedTransactions).orElseGet(Collections::emptyList).stream()
                .map(TransferTransactionEntity::getTransactions)
                .flatMap(Collection::stream)
                .map(TransactionEntity::getId)
                .anyMatch(confirmedId -> Objects.equals(idToConfirm, confirmedId));
    }

    public Optional<TransactionEntity> getRejectedTransfer(String transferId) {
        if (Strings.isNullOrEmpty(transferId)) {
            return Optional.empty();
        }

        return Optional.ofNullable(rejectedTransactions).orElseGet(Collections::emptyList).stream()
                .map(TransferTransactionEntity::getTransactions)
                .flatMap(Collection::stream)
                .filter(transactionEntity -> transactionEntity.getId().equalsIgnoreCase(transferId))
                .findFirst();
    }
}
