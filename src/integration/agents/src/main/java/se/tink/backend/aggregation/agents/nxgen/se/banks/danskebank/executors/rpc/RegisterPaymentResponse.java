package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity.ForcableErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterPaymentResponse extends AbstractResponse {
    @JsonProperty("AutoStartToken")
    private String autoStartToken;

    @JsonProperty("ForcableError")
    private List<ForcableErrorEntity> forcableError;

    @JsonProperty("InitSignPackage")
    private String initSignPackage;

    @JsonProperty("IsConfirmation")
    private boolean isConfirmation;

    @JsonProperty("OrderRef")
    private String orderRef;

    @JsonProperty("ResponseCode")
    private int responseCode;

    @JsonProperty("SignDataEnc")
    private String signDataEnc;

    @JsonProperty("SignatureId")
    private String signatureId;

    @JsonProperty("SignatureText")
    private String signatureText;

    public String getOrderRef() {
        return orderRef;
    }

    public String getAutoStartToken() {
        return autoStartToken;
    }
}
