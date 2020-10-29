package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class GroupEntity {
    private String name;
    private List<GroupAccountEntity> accounts;
}
