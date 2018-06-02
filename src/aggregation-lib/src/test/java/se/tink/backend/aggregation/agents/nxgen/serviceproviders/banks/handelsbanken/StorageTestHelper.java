package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Link;

public class StorageTestHelper {
    static List<Link> createLinks(HandelsbankenConstants.URLS.Links entryPoint, String href) {
        return Lists.newArrayList(new Link()
                .setHref(href)
                .setRel(entryPoint.getName())
        );
    }
}
