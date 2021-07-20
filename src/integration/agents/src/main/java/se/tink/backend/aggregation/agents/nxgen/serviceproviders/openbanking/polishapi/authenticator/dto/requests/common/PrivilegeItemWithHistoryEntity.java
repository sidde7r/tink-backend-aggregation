package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivilegeItemWithHistoryEntity {
    String scopeUsageLimit;
    Integer maxAllowedHistoryLong;
}
