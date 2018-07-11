package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class Status {

    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCod() {
        return this.getCode();
    }

    /**
     * Seems to be a spelling error
     */
    public void setCod(String cod) {
        this.setCode(cod);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
