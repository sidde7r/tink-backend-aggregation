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

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DeleteTransactionAccountsContainer implements TransactionAccountContainer {

    @NotNull
    @ApiModelProperty(value = "Indicating if this a historical batch of transactions or a real time transaction.", allowableValues = TransactionContainerType.DOCUMENTED, example = "REAL_TIME", required = true)
    private TransactionContainerType type;

    @ListNotNullOrEmpty
    @NoNullElements
    @Valid
    @ApiModelProperty(value = "The transaction accounts.", required = true)
    private List<DeleteTransactionAccountEntity> transactionAccounts;

    @AssertTrue(message = "Container type must be REAL_TIME")
    private boolean isRealTimeType() {
        return Objects.equals(type, TransactionContainerType.REAL_TIME);
    }

    public List<DeleteTransactionAccountEntity> getTransactionAccounts() {
        return transactionAccounts;
    }

    public TransactionContainerType getType() {
        return type;
    }

    public void setType(TransactionContainerType type) {
        this.type = type;
    }

    public void setTransactionAccounts(List<DeleteTransactionAccountEntity> transactionAccounts) {
        this.transactionAccounts = transactionAccounts;
    }
}
