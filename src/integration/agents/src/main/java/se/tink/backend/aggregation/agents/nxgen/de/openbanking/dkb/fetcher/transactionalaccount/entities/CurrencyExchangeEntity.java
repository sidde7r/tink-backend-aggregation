package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrencyExchangeEntity {
    private String contractIdentification;
    private String exchangeRate;
    private String quotationDate;
    private String sourceCurrency;
    private String targetCurrency;
    private String unitCurrency;
}
