package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.transaction;

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
