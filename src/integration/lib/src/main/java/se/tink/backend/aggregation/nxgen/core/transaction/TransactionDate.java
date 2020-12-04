package se.tink.backend.aggregation.nxgen.core.transaction;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.models.TransactionDateType;

@Builder
@Getter
@Setter
public class TransactionDate {

    private TransactionDateType type;
    private LocalDateTime value;

    public static se.tink.backend.aggregation.agents.models.TransactionDate toSystemModel(
            TransactionDate source) {
        se.tink.backend.aggregation.agents.models.TransactionDate dest =
                new se.tink.backend.aggregation.agents.models.TransactionDate();
        dest.setType(source.getType());
        dest.setValue(source.getValue());
        return dest;
    }
}
