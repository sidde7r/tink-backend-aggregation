package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc;

import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.MemberEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MembersResponse {
    private List<MemberEntity> members;

    public List<MemberEntity> getMembers() {
        return ListUtils.emptyIfNull(members);
    }
}
