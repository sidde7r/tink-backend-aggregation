package se.tink.backend.aggregation.agents.consent.ukob;

import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class UkObConsentGenerator extends ConsentGenerator {

    public UkObConsentGenerator(CredentialsRequest request, Set<String> availablePermissions) {
        super(request, availablePermissions, new UkObPermissionsMapper());
    }
}
