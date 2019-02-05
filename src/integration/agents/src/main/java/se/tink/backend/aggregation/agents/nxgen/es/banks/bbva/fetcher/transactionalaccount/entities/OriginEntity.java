package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OriginEntity {
    private String detailSourceId;
    private String billCode;
    private String procedureCode;
    private String timeRMS;
    private String dateRMS;
    private String panCode;
    private String detailSourceKey;

    public String getDetailSourceId() {
        return detailSourceId;
    }

    public String getBillCode() {
        return billCode;
    }

    public String getProcedureCode() {
        return procedureCode;
    }

    public String getTimeRMS() {
        return timeRMS;
    }

    public String getDateRMS() {
        return dateRMS;
    }

    public String getPanCode() {
        return panCode;
    }

    public String getDetailSourceKey() {
        return detailSourceKey;
    }
}
