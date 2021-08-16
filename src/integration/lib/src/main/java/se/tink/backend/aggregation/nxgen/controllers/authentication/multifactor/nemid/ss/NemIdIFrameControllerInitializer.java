package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.libraries.i18n.Catalog;

/**
 * This class should be used to correctly initialize {@link NemIdIFrameController} with all its
 * dependencies.
 */
public interface NemIdIFrameControllerInitializer {

    NemIdIFrameController initNemIdIframeController(
            NemIdParametersFetcher nemIdParametersFetcher,
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            MetricContext metricContext,
            AgentTemporaryStorage agentTemporaryStorage);
}
