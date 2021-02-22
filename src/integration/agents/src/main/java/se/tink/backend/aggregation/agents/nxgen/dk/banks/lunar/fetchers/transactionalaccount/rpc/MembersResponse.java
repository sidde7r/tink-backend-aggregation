package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.MemberEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MembersResponse {
    private List<MemberEntity> members;
}
