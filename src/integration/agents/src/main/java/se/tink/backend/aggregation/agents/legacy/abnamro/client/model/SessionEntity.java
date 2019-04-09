package se.tink.backend.aggregation.agents.abnamro.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionEntity {

    private String connectionType;
    private String deviceType;
    private long lastLogonDate;
    private RepresentativeEntity representative;
    private String representedCustomer;
    private String selectedCustomer;

    public String getConnectionType() {
        return connectionType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public long getLastLogonDate() {
        return lastLogonDate;
    }

    public RepresentativeEntity getRepresentative() {
        return representative;
    }

    public String getRepresentedCustomer() {
        return representedCustomer;
    }

    public String getSelectedCustomer() {
        return selectedCustomer;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setLastLogonDate(long lastLogonDate) {
        this.lastLogonDate = lastLogonDate;
    }

    public void setRepresentative(RepresentativeEntity representative) {
        this.representative = representative;
    }

    public void setRepresentedCustomer(String representedCustomer) {
        this.representedCustomer = representedCustomer;
    }

    public void setSelectedCustomer(String selectedCustomer) {
        this.selectedCustomer = selectedCustomer;
    }
}
