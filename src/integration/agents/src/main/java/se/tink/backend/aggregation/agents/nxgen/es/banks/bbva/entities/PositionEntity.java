package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PositionEntity {
    private boolean isIncludedInMySituation;
    private String balanceAgrupation;
    private boolean isLegacy;
    private ContractEntity contract;
    private UserAccessControlEntity userAccessControl;
    private ManagementApplicationEntity managementApplication;

    public boolean isIsIncludedInMySituation() {
        return isIncludedInMySituation;
    }

    public String getBalanceAgrupation() {
        return balanceAgrupation;
    }

    public boolean isIsLegacy() {
        return isLegacy;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public UserAccessControlEntity getUserAccessControl() {
        return userAccessControl;
    }

    public ManagementApplicationEntity getManagementApplication() {
        return managementApplication;
    }
}
