package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.rpc;

import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.entities.ApplicationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditApplicationsResponse {
    private List<ApplicationEntity> applications;

    public List<ApplicationEntity> getApplications() {
        return ListUtils.emptyIfNull(applications);
    }
}
