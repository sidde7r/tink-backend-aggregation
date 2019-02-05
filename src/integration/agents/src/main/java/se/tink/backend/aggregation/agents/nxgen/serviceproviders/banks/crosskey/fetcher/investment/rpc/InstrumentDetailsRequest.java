package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentDetailsRequest {

    private String isinCode;
    private String internalMarket;

    public String getIsinCode() {
        return isinCode;
    }

    public String getInternalMarket() {
        return internalMarket;
    }

    public static InstrumentDetailsRequest of(String isinCode, String internalMarket) {
        InstrumentDetailsRequest request = new InstrumentDetailsRequest();
        request.isinCode = isinCode;
        request.internalMarket = internalMarket;
        return request;
    }
}
