package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import se.tink.libraries.http.annotations.validation.ListNotNullOrEmpty;
import se.tink.libraries.http.annotations.validation.NoNullElements;

/**
 * This class is API compatible with CreateTransactionAccountContainer but is used for synchronous updates where we
 * take in exactly one account and one transaction to update rather than allowing any number.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UpdateTransactionAccountContainer implements TransactionAccountContainer {

    @NotNull
    @ApiModelProperty(value = "Indicating if this a historical batch of transactions or a real time transaction.",
            allowableValues = "REAL_TIME", example = "REAL_TIME", required = true)
    private TransactionContainerType type;

    @ListNotNullOrEmpty
    @NoNullElements
    @Valid
    @ApiModelProperty(value = "The transaction accounts.", required = true)
    private List<UpdateTransactionAccountEntity> transactionAccounts;

    @AssertTrue(message = "TransactionAccounts must contain exactly one element.")
    private boolean isOneElementOnlyInTransactions() {
        return transactionAccounts.size() == 1;
    }

    @AssertTrue(message = "Container type must be REAL_TIME")
    private boolean isRealTimeType() {
        return Objects.equals(type, TransactionContainerType.REAL_TIME);
    }

    public List<UpdateTransactionAccountEntity> getTransactionAccounts() {
        return transactionAccounts;
    }

    public TransactionContainerType getType() {
        return type;
    }
}
