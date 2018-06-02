package se.tink.backend.rpc;

import java.util.List;

public class DataExportRequestListResponse {
    private List<DataExportRequest> dataExportRequests;

    public DataExportRequestListResponse(List<DataExportRequest> dataExportRequests) {
        this.dataExportRequests = dataExportRequests;
    }

    public List<DataExportRequest> getDataExportRequests() {
        return dataExportRequests;
    }

    public void setDataExportRequests(List<DataExportRequest> dataExportRequests) {
        this.dataExportRequests = dataExportRequests;
    }
}
