package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

/**
 * This class should be used to correctly initialize {@link
 * se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController}
 * with all its dependencies.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NemIdIFrameControllerInitializer {

    public static NemIdIFrameController initNemIdIframeController(
            NemIdParametersFetcher nemIdParametersFetcher,
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            MetricContext metricContext) {

        NemIdSSIFrameModule nemIdSSIFrameModule =
                new NemIdSSIFrameModule(
                        nemIdParametersFetcher,
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        metricContext);

        Injector injector = Guice.createInjector(nemIdSSIFrameModule);
        return injector.getInstance(NemIdIFrameController.class);
    }
}
