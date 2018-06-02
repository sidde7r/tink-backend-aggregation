package se.tink.backend.core.application;

import java.util.Set;
import se.tink.libraries.application.ApplicationType;

public class EligibleApplicationTypesResponse {
    private Set<ApplicationType> eligibleApplicationTypes;

    public Set<ApplicationType> getEligibleApplicationTypes() {
        return eligibleApplicationTypes;
    }

    public void setEligibleApplicationTypes(Set<ApplicationType> eligibleApplicationTypes) {
        this.eligibleApplicationTypes = eligibleApplicationTypes;
    }
}
