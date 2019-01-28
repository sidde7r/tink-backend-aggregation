package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.libraries.transfer.enums.TransferType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SebRequest {

    @JsonProperty("request")
    public Payload request = new Payload();

    public static SebRequest createWithTransfer(TransferType transferType, SebTransferRequestEntity externalTransfer) {
        SebRequest createRequest = new SebRequest();
        createRequest.request.ServiceInput = null;
        createRequest.request.VODB = new VODB();
        createRequest.request.VODB.setTransfer(transferType, externalTransfer);
        return createRequest;
    }

    public static Builder empty() {
        return new Builder();
    }

    public static Builder withSEB_KUND_NR(String customerId) {
        return new Builder("SEB_KUND_NR", customerId);
    }

    public static Builder withUSER_ID(String customerId) {
        return new Builder("USER_ID", customerId);
    }

    public static class Builder {
        private final List<ServiceInput> serviceInputs;

        private Builder(String variableName, String customerId) {
            serviceInputs = Lists.newArrayList();
            addServiceInputEQ(variableName, customerId);
        }

        public Builder() {
            serviceInputs = Lists.newArrayList();
        }

        public SebRequest build() {
            SebRequest sebRequest = new SebRequest();
            sebRequest.request.ServiceInput = serviceInputs;
            return sebRequest;
        }

        public Builder addServiceInputEQ(String variableName, String variableValue) {
            ServiceInput serviceInput = new ServiceInput(variableName, variableValue);
            serviceInputs.add(serviceInput);
            return this;
        }

        public Builder addServiceInputEQ(String variableName, Integer variableValue) {
            ServiceInput serviceInput = new ServiceInput(variableName, variableValue);
            serviceInputs.add(serviceInput);
            return this;
        }
    }
}
