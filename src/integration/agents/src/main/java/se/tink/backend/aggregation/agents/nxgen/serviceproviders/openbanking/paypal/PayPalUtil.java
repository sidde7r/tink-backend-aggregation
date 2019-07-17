package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared.LinkEntity;

public class PayPalUtil {
    public static Optional<LinkEntity> findByRelation(List<LinkEntity> links, String type) {
        return links.stream()
                .filter(linkEntity -> linkEntity.getRelation().equalsIgnoreCase(type))
                .findFirst();
    }
}
