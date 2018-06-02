package se.tink.backend.export.client;

import java.util.List;
import se.tink.backend.export.GdprExportService;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;

public class ClientGdprExportServiceFactory implements GdprExportServiceFactory {
    private ServiceClassBuilder builder;

    public ClientGdprExportServiceFactory(ServiceClassBuilder builder) {
        this.builder = builder;
    }

    public ClientGdprExportServiceFactory(List<String> pinnedCertificates, String url) {
        this(new BasicWebServiceClassBuilder(
                new InterContainerJerseyClientFactory(pinnedCertificates).build().resource(url)));
    }

    @Override
    public GdprExportService getGdprExportService() {
        return builder.build(GdprExportService.class);
    }

}
