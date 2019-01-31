package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.encryption.MessageContainer;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers.BankIdResourceHelper;

public class BankIdRequest<MI> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("Input")
    private BankIdInput<MI> input;

    public BankIdRequest() {

    }

    public BankIdRequest(MI moduleInput) {
        input = new BankIdInput<>(moduleInput);
    }

    public BankIdInput<MI> getInput() {
        return input;
    }

    public void setInput(BankIdInput<MI> input) {
        this.input = input;
    }

    public MessageContainer encrypt(BankIdResourceHelper bankIdResourceHelper) {
        try {
            String encryptedMessage = bankIdResourceHelper.getEncryptionHelper().m12076a(MAPPER.writeValueAsBytes(this));
            return new MessageContainer(encryptedMessage);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
