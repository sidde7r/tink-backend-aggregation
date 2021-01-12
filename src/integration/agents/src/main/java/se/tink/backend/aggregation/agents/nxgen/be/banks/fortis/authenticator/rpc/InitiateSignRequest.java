package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import java.util.List;
import lombok.Builder;
import lombok.Singular;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.SignDataEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.TransactionDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
public class InitiateSignRequest {
    private final List<TransactionDataEntity> transactionDatas;
    private final String applicationCode;
    private final String orderType;

    @Singular private final List<SignDataEntity> signDatas;
}
