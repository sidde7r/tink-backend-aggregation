package se.tink.backend.aggregation.agents.banks.danskebank.v2.encryption;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.json.JSONException;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers.BankIdResourceHelper;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.ServiceStatusEntity;

public class MessageContainer {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String message;

    @JsonProperty(value = "ServiceStatus", access = JsonProperty.Access.WRITE_ONLY)
    private ServiceStatusEntity serviceStatus;

    public MessageContainer() {}

    public ServiceStatusEntity getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatusEntity serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public MessageContainer(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public <T extends BankIdResponse> T decrypt(
            BankIdResourceHelper bankIdResourceHelper, Class<T> toInstanceType) {
        try {
            String decryptedMessage = bankIdResourceHelper.getEncryptionHelper().m12075a(message);
            return MAPPER.readValue(decryptedMessage, toInstanceType);
        } catch (JSONException | IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
