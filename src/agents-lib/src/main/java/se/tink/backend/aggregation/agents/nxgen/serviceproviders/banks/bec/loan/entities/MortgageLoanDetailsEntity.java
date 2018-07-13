package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MortgageLoanDetailsEntity {
    // `groupName` is not always present.
    private String groupName;
    private List<DetailsInGroupEntity> detailsInGroup;

    public String getGroupName() {
        return groupName;
    }

    public List<DetailsInGroupEntity> getDetailsInGroup() {
        return detailsInGroup;
    }
}
