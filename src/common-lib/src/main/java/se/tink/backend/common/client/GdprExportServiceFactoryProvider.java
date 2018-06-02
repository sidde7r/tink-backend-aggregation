package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.annotation.Nullable;
import se.tink.backend.export.client.ClientGdprExportServiceFactory;
import se.tink.backend.export.client.GdprExportServiceFactory;
import se.tink.backend.guice.annotations.ExportUserDataConfiguration;
import se.tink.libraries.endpoints.EndpointConfiguration;

public class GdprExportServiceFactoryProvider implements Provider<GdprExportServiceFactory> {
    private final EndpointConfiguration endpoint;

    @Inject
    public GdprExportServiceFactoryProvider(@Nullable @ExportUserDataConfiguration EndpointConfiguration endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public GdprExportServiceFactory get() {
        if (endpoint == null) {
            return null;
        }
        return new ClientGdprExportServiceFactory(endpoint.getPinnedCertificates(), endpoint.getUrl());
    }
}

