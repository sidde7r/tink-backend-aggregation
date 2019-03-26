package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.entities;

public final class QueryParameterEntity {
    private PayloadEntity payloadEntity;
    private String procedure;

    public void setPayloadEntity(PayloadEntity payloadEntity) {
        this.payloadEntity = payloadEntity;
    }

    public PayloadEntity getPayload() {
        return payloadEntity;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getProcedure() {
        return procedure;
    }
}
