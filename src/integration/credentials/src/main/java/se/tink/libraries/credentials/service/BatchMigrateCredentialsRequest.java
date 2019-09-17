package se.tink.libraries.credentials.service;

import java.util.List;

public class BatchMigrateCredentialsRequest {

    private List<MigrateCredentialsRequest> requestList;
    private int targetVersion;

    public List<MigrateCredentialsRequest> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<MigrateCredentialsRequest> requestList) {
        this.requestList = requestList;
    }

    public int getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(int targetVersion) {
        this.targetVersion = targetVersion;
    }
}
