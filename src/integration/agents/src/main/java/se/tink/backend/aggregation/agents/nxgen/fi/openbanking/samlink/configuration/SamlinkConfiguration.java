package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SamlinkConfiguration extends BerlinGroupConfiguration {

    private String subscriptionKey;

    public String getSubscriptionKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(subscriptionKey),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Subscription key"));

        return subscriptionKey;
    }
}
