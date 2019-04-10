package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Link;

public class StorageTestHelper {
    static Map<String, Link> createLinks(
            HandelsbankenConstants.URLS.Links entryPoint, String href) {
        Map<String, Link> links = new HashMap<>();
        links.put(entryPoint.getName(), new Link().setHref(href));
        return links;
    }
}
