package se.tink.backend.aggregation.agents.standalone;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class GenericAgentConfiguration implements ClientConfiguration {

    @Secret
    @JsonProperty("grpc_host")
    private String grpcHost;

    @Secret
    @JsonProperty("grpc_port")
    private String grpcPort;

    @JsonProperty private String bankCode;

    public String getGrpcHost() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(grpcHost),
                String.format(
                        GenericAgentConstants.ErrorMessages.INVALID_CONFIGURATION, "grpc host"));
        return grpcHost;
    }

    public int getGrpcPort() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(grpcPort),
                String.format(
                        GenericAgentConstants.ErrorMessages.INVALID_CONFIGURATION, "grpc port"));
        return Integer.parseInt(grpcPort);
    }

    public String getBankCode() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(bankCode),
                String.format(
                        GenericAgentConstants.ErrorMessages.INVALID_CONFIGURATION, "bankCode"));

        return bankCode;
    }
}
