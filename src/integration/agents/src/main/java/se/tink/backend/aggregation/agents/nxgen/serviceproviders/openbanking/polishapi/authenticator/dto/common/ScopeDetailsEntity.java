package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuperBuilder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ScopeDetailsEntity {
    List<PrivilegeListEntity> privilegeList;
    String scopeGroupType;
    Integer scopeTimeDuration;
    String scopeTimeLimit;
    String consentId;
    String throttlingPolicy;

    // available only in response
    ResourceEntity resource;
}
