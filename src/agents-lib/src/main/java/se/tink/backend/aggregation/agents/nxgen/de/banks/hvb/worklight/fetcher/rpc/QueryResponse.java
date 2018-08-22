package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.entities.PayloadEntity;

public final class QueryResponse {
    private PayloadEntity response;
    private boolean isSuccessful;

    public void setResponse(PayloadEntity response) {
        this.response = response;
    }

    public void setIsSuccessful(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    public boolean getIsSuccessful() {
        return isSuccessful;
    }

    public String getCipherText() {
        return response.getA();
    }

    public String getSignature() {
        return response.getB();
    }
}
