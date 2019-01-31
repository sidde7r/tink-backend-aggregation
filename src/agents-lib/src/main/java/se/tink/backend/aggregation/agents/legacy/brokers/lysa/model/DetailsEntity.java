package se.tink.backend.aggregation.agents.brokers.lysa.model;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailsEntity {
    private List<AccountEntity> accounts;
    private List<LegalEntityEntity> legalEntities;
    private String legalEntityId;
    private String loginName;
    private String name;
    private String presentationName;
    private String entityType;
    private String trackId;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public List<LegalEntityEntity> getLegalEntities() {
        return legalEntities;
    }

    public void setLegalEntities(List<LegalEntityEntity> legalEntities) {
        this.legalEntities = legalEntities;
    }

    public String getLegalEntityId() {
        return legalEntityId;
    }

    public void setLegalEntityId(String legalEntityId) {
        this.legalEntityId = legalEntityId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }
}
