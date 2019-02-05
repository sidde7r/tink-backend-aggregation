package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AksjerOverview {
    private String id;
    private String type;
    private String givenName;
    private String surName;
    private List<String> permissions;
    private CustomerDataEntity customerData;
    private String takeover;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurName() {
        return surName;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public CustomerDataEntity getCustomerData() {
        return customerData;
    }

    public String getTakeover() {
        return takeover;
    }
}
