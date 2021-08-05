package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountEntity {

    private String accountUid;
    private String defaultCategory;
    private String currency;
    private String createdAt;
    private String name;
}
