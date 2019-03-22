package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OperationDatesEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("fechaValor")
    private Date dateValue;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("fechaOperacion")
    private Date oprationDate;
}
