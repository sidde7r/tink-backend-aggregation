package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountEntity {

    private String accountUid;
    private String defaultCategory;
    private String currency;
    private LocalDateTime createdAt;
    private String name;
}
