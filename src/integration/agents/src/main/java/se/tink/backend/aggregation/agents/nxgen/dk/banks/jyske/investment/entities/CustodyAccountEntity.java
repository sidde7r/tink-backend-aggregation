package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustodyAccountEntity {
    private IdEntity id;
    private String name;
    private String ownerName;
    private long ownerRefNo;
    private Boolean owner;
    private String ownership;
    private String group;
    private Double marketValue;
    private Boolean tradesAllowed;

    public IdEntity getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getOwnerRefNo() {
        return ownerRefNo;
    }

    public Boolean getOwner() {
        return owner;
    }

    public String getOwnership() {
        return ownership;
    }

    public String getGroup() {
        return group;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public Boolean getTradesAllowed() {
        return tradesAllowed;
    }

    public String createUniqueIdentifier() {
        return IdEntity.createUniqueIdentifier(id);
    }
}
