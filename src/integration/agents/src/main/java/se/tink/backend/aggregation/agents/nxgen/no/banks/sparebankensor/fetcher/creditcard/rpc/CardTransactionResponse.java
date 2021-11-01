package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.entities.CardTranscationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CardTransactionResponse {
    @JsonProperty("list")
    private List<CardTranscationEntity> cardTransactions;
}
