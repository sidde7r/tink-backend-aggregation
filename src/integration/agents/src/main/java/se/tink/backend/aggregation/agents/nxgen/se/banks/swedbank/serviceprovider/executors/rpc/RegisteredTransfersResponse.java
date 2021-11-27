package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
@Getter
public class RegisteredTransfersResponse {
    private LinksEntity links;
    private String amount;
    private CountEntity remainingEinvoices;
    private List<TransferTransactionEntity> registeredTransactions;

    public void oneUnsignedTransferOrThrow() {
        List<TransferTransactionEntity> registeredTransactionEntities =
                Optional.ofNullable(registeredTransactions).orElseGet(Collections::emptyList);

        if (registeredTransactionEntities.size() != 1) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS
                                    .getKey()
                                    .get())
                    .setMessage(
                            SwedbankBaseConstants.ErrorMessage.NOT_EXACTLY_ONE_UNSIGNED_TRANSFER)
                    .setInternalStatus(InternalStatus.EXISTING_UNSIGNED_TRANSFERS.toString())
                    .build();
        }
    }

    public Optional<String> getIdToConfirm() {
        return Optional.ofNullable(registeredTransactions).orElseGet(Collections::emptyList)
                .stream()
                .map(TransferTransactionEntity::getTransactions)
                .flatMap(Collection::stream)
                .map(TransactionEntity::getId)
                .findFirst();
    }
}
