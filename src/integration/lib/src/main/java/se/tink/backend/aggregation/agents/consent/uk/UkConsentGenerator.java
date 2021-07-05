package se.tink.backend.aggregation.agents.consent.uk;

import se.tink.backend.aggregation.agents.consent.ConsentGenerator;

public class UkConsentGenerator extends ConsentGenerator {

    public UkConsentGenerator() {
        super(new UkPermissionsMapper());
    }
}
