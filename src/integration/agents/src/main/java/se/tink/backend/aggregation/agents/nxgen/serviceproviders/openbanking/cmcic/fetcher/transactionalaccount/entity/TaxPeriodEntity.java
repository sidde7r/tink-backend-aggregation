package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class TaxPeriodEntity {
    @JsonProperty("year")
    private String year = null;

    @JsonProperty("type")
    private TaxRecordPeriodCodeEntity type = null;

    @JsonProperty("fromDate")
    private LocalDate fromDate = null;

    @JsonProperty("toDate")
    private LocalDate toDate = null;
}
