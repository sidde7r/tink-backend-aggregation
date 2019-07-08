package se.tink.backend.aggregation.agents.nxgen.it.openbanking.credem.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;

public class CredemConfiguration extends CbiGlobeConfiguration {

    private String psuIdType;

    public String getPsuIdType() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIdType),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Psu ID Type"));

        return psuIdType;
    }
}
