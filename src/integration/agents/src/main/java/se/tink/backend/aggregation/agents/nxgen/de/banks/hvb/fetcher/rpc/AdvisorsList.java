package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.rpc;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities.AdvisorProfileEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class AdvisorsList {
    private static final Logger logger = LoggerFactory.getLogger(AdvisorsList.class);

    private List<AdvisorProfileEntity> advisorProfiles;

    public Optional<String> getAccountOwner() {
        if (advisorProfiles == null) {
            logger.warn("Could not find account holder name");
            return Optional.empty();
        }

        final List<String> names = advisorProfiles.stream()
                .map(AdvisorProfileEntity::getCustomerName)
                .filter(Objects::nonNull)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());

        if (names.isEmpty()) {
            return Optional.empty();
        } else if (names.size() >= 2) {
            logger.warn("Found more than one customer name; account holder name may not be correct");
        }

        return Optional.ofNullable(names.get(0));
    }
}
