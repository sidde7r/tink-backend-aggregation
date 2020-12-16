package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class SummaryEntity {
    private Double totalAmount;
}
