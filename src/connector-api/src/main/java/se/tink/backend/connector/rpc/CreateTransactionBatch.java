package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import se.tink.libraries.http.annotations.validation.ListNotNullOrEmpty;
import se.tink.libraries.http.annotations.validation.NoNullElements;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CreateTransactionBatch {
    private final static int MAX_TRANSACTIONS = 2500;

    @ListNotNullOrEmpty
    @NoNullElements
    @Valid
    @ApiModelProperty(value = "The batch entities. May contain a maximum of " + MAX_TRANSACTIONS + " entities per request.", required = true)
    private List<IngestTransactionEntity> ingestEntities;

    @AssertTrue(message = "All accounts must have different externalIDs and accumulated number of transactions must be below " + MAX_TRANSACTIONS)
    private boolean isUniqueExternalIdAndWithinLimit() {
        AtomicInteger trxSize = new AtomicInteger(0);
        Set<String> uniqueIds = ingestEntities.stream()
                .flatMap(e -> e.getContainer().getTransactionAccounts().stream())
                .flatMap(a -> a.getTransactions().stream())
                .peek(e -> trxSize.incrementAndGet())
                .map(CreateTransactionEntity::getExternalId)
                .collect(Collectors.toSet());

        return uniqueIds.size() == trxSize.get() && trxSize.get() <= MAX_TRANSACTIONS;
    }

    public List<IngestTransactionEntity> getIngestEntities() {
        return ingestEntities;
    }

    public void setIngestEntities(List<IngestTransactionEntity> ingestEntities) {
        this.ingestEntities = ingestEntities;
    }
}
