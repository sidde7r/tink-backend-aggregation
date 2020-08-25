package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListPayeesRequest {
    private String languageCode;

    private ListPayeesRequest(String languageCode) {
        this.languageCode = languageCode;
    }

    public static ListPayeesRequest create(String languageCode) {
        return new ListPayeesRequest(languageCode);
    }
}
