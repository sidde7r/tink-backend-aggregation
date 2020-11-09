package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class RoleEntity {
    private String name;
    private String role;
}
