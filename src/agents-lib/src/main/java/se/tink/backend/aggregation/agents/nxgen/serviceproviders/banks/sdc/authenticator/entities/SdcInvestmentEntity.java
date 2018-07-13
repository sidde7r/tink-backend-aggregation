package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcInvestmentEntity {

    private boolean deposit;

//    Other possible fields:
//    private boolean realtime;
//    private boolean security;
//    private boolean trade;
//    private boolean orderBook;

    public boolean isDeposit() {
        return deposit;
    }
}
