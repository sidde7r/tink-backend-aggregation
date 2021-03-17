package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@Getter
public class TransactionEntity {
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
}
