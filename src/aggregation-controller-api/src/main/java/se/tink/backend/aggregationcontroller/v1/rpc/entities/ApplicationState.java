package se.tink.backend.aggregationcontroller.v1.rpc.entities;

import java.util.HashMap;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.ApplicationPropertyKey;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.ApplicationStatusKey;

public class ApplicationState {
    private ApplicationStatusKey newApplicationStatus;
    private HashMap<ApplicationPropertyKey, Object> applicationProperties;

    public ApplicationStatusKey getNewApplicationStatus() {
        return newApplicationStatus;
    }

    public void setNewApplicationStatus(
            ApplicationStatusKey newApplicationStatus) {
        this.newApplicationStatus = newApplicationStatus;
    }

    public HashMap<ApplicationPropertyKey, Object> getApplicationProperties() {
        return applicationProperties;
    }

    public void setApplicationProperties(
            HashMap<ApplicationPropertyKey, Object> applicationProperties) {
        this.applicationProperties = applicationProperties;
    }
}
