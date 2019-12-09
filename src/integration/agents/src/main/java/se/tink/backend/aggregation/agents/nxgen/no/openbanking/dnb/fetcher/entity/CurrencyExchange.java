package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrencyExchange {
    private String contractIdentification;
    private String exchangeRate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date quotationDate;

    private String sourceCurrency;
    private String targetCurrency;
    private String unitCurrency;
}
