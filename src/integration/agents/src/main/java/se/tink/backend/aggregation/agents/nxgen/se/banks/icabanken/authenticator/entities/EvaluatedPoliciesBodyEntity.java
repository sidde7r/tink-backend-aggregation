package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(UpperCamelCaseStrategy.class)
public class EvaluatedPoliciesBodyEntity {
    private static final Logger log = LoggerFactory.getLogger(EvaluatedPoliciesBodyEntity.class);
    private List<PolicyEntity> policies;

    @JsonIgnore
    public Set<String> getOkPolicies() {
        if (policies == null || policies.isEmpty()) {
            log.warn("List of evaluated policies is null or empty.");
            return Collections.emptySet();
        }

        return policies.stream()
                .filter(PolicyEntity::isOk)
                .map(PolicyEntity::getName)
                .collect(Collectors.toSet());
    }
}
