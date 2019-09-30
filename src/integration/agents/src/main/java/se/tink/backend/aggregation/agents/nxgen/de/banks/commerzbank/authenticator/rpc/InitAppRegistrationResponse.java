package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.InitAppRegistrationEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitAppRegistrationResponse extends BaseResponse {
    private ResultEntity<InitAppRegistrationEntity> result;

    @JsonIgnore
    public String getAppId() {
        List<InitAppRegistrationEntity> resultItems = result.getItems();
        if (resultItems.size() != 1 || Strings.isNullOrEmpty(resultItems.get(0).getAppId())) {
            throw new IllegalStateException(
                    "Could not get appId which is required for registration.");
        }

        return resultItems.get(0).getAppId();
    }
}
