package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JointAccountHolderResponse {

    private String accountHolderUid;
    private AccountHolder personOne;
    private AccountHolder personTwo;

    public String getCombinedFullName() {
        return personOne.getFullName() + " / " + personTwo.getFullName();
    }
}
