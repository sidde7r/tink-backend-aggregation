package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.ImageRecognizeHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.ImageRecognizerHelperImpl;

public final class ImageRecognizerHelperModule extends AbstractModule {

    @Singleton
    @Provides
    public ImageRecognizeHelper provideImageRecognizeHelper() {
        return new ImageRecognizerHelperImpl();
    }
}
