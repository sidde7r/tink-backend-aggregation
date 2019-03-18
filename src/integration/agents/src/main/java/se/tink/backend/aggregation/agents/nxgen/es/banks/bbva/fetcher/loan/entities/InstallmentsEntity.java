package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstallmentsEntity {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date installmentDate;
    private AmountEntity installmentAmount;
}
