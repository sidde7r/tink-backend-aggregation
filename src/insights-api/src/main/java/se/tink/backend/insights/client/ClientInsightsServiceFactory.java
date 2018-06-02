package se.tink.backend.insights.client;

import java.util.List;
import se.tink.backend.insights.http.InsightService;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;

public class ClientInsightsServiceFactory implements InsightsServiceFactory {
    public static String SERVICE_NAME = "insights";
    private ServiceClassBuilder builder;

    public ClientInsightsServiceFactory(ServiceClassBuilder builder) {
        this.builder = builder;
    }

    public ClientInsightsServiceFactory(List<String> pinnedCertificates, String url) {
        this(new BasicWebServiceClassBuilder(new InterContainerJerseyClientFactory(pinnedCertificates).build().resource(url)));
    }

    @Override
    public InsightService getInsightsService() {
        return builder.build(InsightService.class);
    }

}
