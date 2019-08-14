package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.Payload;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.RequestComponent;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.ServiceInput;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.UserCredentials;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Request {

    @JsonProperty("request")
    private Payload request;

    public Request() {}

    @JsonIgnore
    private Request(Builder builder) {
        request = new Payload(builder.components);
    }

    public static class Builder {
        private final List<RequestComponent> components;

        public Builder() {
            components = Lists.newArrayList();
        }

        public Request build() {
            return new Request(this);
        }

        public Builder addServiceInput(String variableName, String variableValue) {
            return addComponent(new ServiceInput(variableName, variableValue));
        }

        public Builder addServiceInput(String variableName, Integer variableValue) {
            return addComponent(new ServiceInput(variableName, variableValue));
        }

        public Builder withUserCredentials(String userId) {
            return addComponent(new UserCredentials(userId));
        }

        public Builder addComponent(RequestComponent component) {
            components.add(component);
            return this;
        }
    }
}
