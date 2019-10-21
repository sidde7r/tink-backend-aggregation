package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.util.List;

public class ApiResponse<T> {

    private final List<T> businessData;
    private final String code;
    private final String message;

    public ApiResponse(List<T> businessData, String code, String message) {
        this.businessData = businessData;
        this.code = code;
        this.message = message;
    }

    public List<T> getBusinessData() {
        return businessData;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
