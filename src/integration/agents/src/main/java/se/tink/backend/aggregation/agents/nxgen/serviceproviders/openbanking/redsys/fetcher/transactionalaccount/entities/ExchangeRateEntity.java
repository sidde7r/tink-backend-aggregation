package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExchangeRateEntity {
    @JsonProperty private String sourceCurrency;
    @JsonProperty private String exchangeRate;
    @JsonProperty private String unitCurrency;
    @JsonProperty private String targetCurrency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date quotationDate;

    @JsonProperty private String contractIdentification;
}
