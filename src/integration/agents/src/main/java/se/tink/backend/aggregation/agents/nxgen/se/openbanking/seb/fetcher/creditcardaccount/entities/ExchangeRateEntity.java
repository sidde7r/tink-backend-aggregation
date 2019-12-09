package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.creditcardaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExchangeRateEntity {

    private String currencyFrom;
    private double rate;
    private String currencyTo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date rateDate;
}
