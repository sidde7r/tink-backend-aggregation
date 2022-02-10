package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator;

import lombok.Data;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.libraries.i18n_aggregation.Catalog;

@Data
public class NemIdIframeAttributes {

    private final NemIdIFrameControllerInitializer iFrameControllerInitializer;
    private final LunarNemIdParametersFetcher parametersFetcher;
    private final Catalog catalog;
    private final StatusUpdater statusUpdater;
    private final SupplementalInformationController supplementalInformationController;
    private final MetricContext metricContext;
    private final Credentials credentials;
    private final AgentTemporaryStorage agentTemporaryStorage;

    public NemIdIFrameController getNemIdIFrameController() {
        return iFrameControllerInitializer.initNemIdIframeController(
                parametersFetcher,
                NemIdCredentialsProvider.defaultProvider(),
                catalog,
                statusUpdater,
                supplementalInformationController,
                metricContext,
                agentTemporaryStorage);
    }
}
