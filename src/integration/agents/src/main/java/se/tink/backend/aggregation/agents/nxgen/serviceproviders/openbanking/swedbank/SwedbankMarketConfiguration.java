package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank;

import java.util.Set;
import se.tink.backend.aggregation.agents.consent.generators.se.swedbank.SwedbankScope;

public interface SwedbankMarketConfiguration {
    String getBIC();

    String getAuthenticationMethodId();

    String getBookingStatus();

    Set<SwedbankScope> getScopes();
}
