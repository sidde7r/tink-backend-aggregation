package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import se.tink.libraries.http.annotations.validation.ListNotNullOrEmpty;
import se.tink.libraries.http.annotations.validation.NoNullElements;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CreateTransactionAccountContainer implements TransactionAccountContainer {
    private final static int MAX_TRANSACTIONS = 2500;

    @NotNull
    @ApiModelProperty(value = "Indicating if this a historical batch of transactions or a real time transaction.", allowableValues = TransactionContainerType.DOCUMENTED, example = "REAL_TIME", required = true)
    private TransactionContainerType type;

    @ListNotNullOrEmpty
    @NoNullElements
    @Valid
    @ApiModelProperty(value = "The transaction accounts. All accounts accumulated may contain a maximum of " + MAX_TRANSACTIONS + " transactions per request.", required = true)
    private List<CreateTransactionAccountEntity> transactionAccounts;

    @AssertTrue(message = "All accounts must have different externalIDs when ingesting transactions.")
    private boolean isUniqueExternalId() {
        Set<String> uniqueIds = transactionAccounts.stream().map(CreateTransactionAccountEntity::getExternalId).collect(
                Collectors.toSet());
        return uniqueIds.size() == transactionAccounts.size();
    }

    @AssertFalse(message = "Too many transactions received. The maximum allowed is " + MAX_TRANSACTIONS)
    private boolean isTooManyTransactions() {
        int transactions = transactionAccounts.stream()
                .map(CreateTransactionAccountEntity::getTransactions)
                .filter(Objects::nonNull)
                .mapToInt(List::size)
                .sum();
        return transactions > MAX_TRANSACTIONS;
    }

    public List<CreateTransactionAccountEntity> getTransactionAccounts() {
        return transactionAccounts;
    }

    public TransactionContainerType getType() {
        return type;
    }

    public void setTransactionAccounts(List<CreateTransactionAccountEntity> transactionAccounts) {
        this.transactionAccounts = transactionAccounts;
    }

    public void setType(TransactionContainerType type) {
        this.type = type;
    }
}
