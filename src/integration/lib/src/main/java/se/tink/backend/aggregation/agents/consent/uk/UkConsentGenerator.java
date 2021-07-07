package se.tink.backend.aggregation.agents.consent.uk;

import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class UkConsentGenerator extends ConsentGenerator {

    public UkConsentGenerator(CredentialsRequest request, Set<String> availablePermissions) {
        super(request, availablePermissions, new UkPermissionsMapper());
    }
}
