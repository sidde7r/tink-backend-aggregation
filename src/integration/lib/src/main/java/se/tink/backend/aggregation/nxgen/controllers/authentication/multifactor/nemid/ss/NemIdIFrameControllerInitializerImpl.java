package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import com.google.inject.Guice;
import com.google.inject.Injector;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.libraries.i18n_aggregation.Catalog;

public class NemIdIFrameControllerInitializerImpl implements NemIdIFrameControllerInitializer {

    @Override
    public NemIdIFrameController initNemIdIframeController(
            NemIdParametersFetcher nemIdParametersFetcher,
            NemIdCredentialsProvider credentialsProvider,
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            MetricContext metricContext,
            AgentTemporaryStorage agentTemporaryStorage) {

        NemIdSSIFrameModule nemIdSSIFrameModule =
                NemIdSSIFrameModule.initializeModule(
                        nemIdParametersFetcher,
                        credentialsProvider,
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        metricContext,
                        agentTemporaryStorage);
        Injector injector = Guice.createInjector(nemIdSSIFrameModule);
        return injector.getInstance(NemIdIFrameController.class);
    }
}
