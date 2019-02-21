package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ShowFundsBalanceEntity {
    private boolean permiteVerFondosSaldoNoCero;
    private boolean permiteVerFondosSaldoCero;
}
