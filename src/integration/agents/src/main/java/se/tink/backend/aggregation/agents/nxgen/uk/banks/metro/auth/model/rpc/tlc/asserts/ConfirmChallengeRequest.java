package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.asserts;

import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.UserIdHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.BaseTlcRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode(callSuper = true)
public class ConfirmChallengeRequest extends BaseTlcRequest<AssertionEntity, UserIdHeaderEntity> {

    public ConfirmChallengeRequest(UserIdHeaderEntity header, AssertionEntity data) {
        super(header, data);
    }
}
