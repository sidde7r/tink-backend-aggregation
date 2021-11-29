package se.tink.agent.sdk.models.transaction;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import se.tink.agent.sdk.models.Amount;

@Builder
@Getter
public class Transaction {
    @Singular private final List<TransactionDate> dates;
    private final String description;
    private final Amount amount;
}
