package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator;

import lombok.Data;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.libraries.i18n.Catalog;

@Data
public class NemIdIframeControllerAttributes {
    private final Catalog catalog;
    private final StatusUpdater statusUpdater;
    private final SupplementalRequester supplementalRequester;
    private final MetricContext metricContext;
    private final Credentials credentials;
}
