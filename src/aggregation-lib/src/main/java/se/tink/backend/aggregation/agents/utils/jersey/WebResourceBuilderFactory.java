package se.tink.backend.aggregation.agents.utils.jersey;

import com.sun.jersey.api.client.WebResource;

public interface WebResourceBuilderFactory {

    WebResource.Builder createWebResourceBuilder(String url) throws Exception;
}
