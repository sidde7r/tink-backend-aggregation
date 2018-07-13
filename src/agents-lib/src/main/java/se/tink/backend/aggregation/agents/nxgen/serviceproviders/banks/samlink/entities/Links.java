package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities;

import java.util.ArrayList;
import java.util.Optional;

public class Links extends ArrayList<LinkEntity> {
    public String getLinkPath(String relKey) {
        return findLink(relKey)
                .orElseGet(LinkEntity::new)
                .getHref();
    }

    public Optional<LinkEntity> findLink(String relKey) {
        return this.stream()
                .filter(l -> relKey.equalsIgnoreCase(l.getRel()))
                .findFirst();
    }
}
