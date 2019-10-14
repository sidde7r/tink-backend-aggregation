package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.util.List;

public class ApiResponse<T> {

    private final List<T> businessData;
    private final String controlData;
    private final String message;

    public ApiResponse(List<T> businessData, String controlData, String message) {
        this.businessData = businessData;
        this.controlData = controlData;
        this.message = message;
    }

    public List<T> getBusinessData() {
        return businessData;
    }

    public String getControlData() {
        return controlData;
    }

    public String getMessage() {
        return message;
    }
}
