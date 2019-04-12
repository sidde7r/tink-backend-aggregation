package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExchangeRateEntity {

    private String currencyFrom;
    private String currencyTo;
    private Double rate;
    private String rateDate;
}
