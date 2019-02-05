package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DisposableAccountsEntity {
    private List<Object> investorsWithAccessToMe;
    private List<Object> accessToInvestors;

    public List<Object> getInvestorsWithAccessToMe() {
        return investorsWithAccessToMe;
    }

    public List<Object> getAccessToInvestors() {
        return accessToInvestors;
    }
}
