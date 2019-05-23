package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

@JsonObject
public class ErrorResponse {
    @JsonProperty("Error")
    private List<ErrorEntity> error;

    public static List<String> getErrorDescriptionsFromException(
            HttpResponseException httpResponseException) {
        if (httpResponseException.getResponse().hasBody()) {
            try {
                return httpResponseException
                        .getResponse()
                        .getBody(ErrorResponse.class)
                        .getErrorDescriptions();
            } catch (HttpClientException | HttpResponseException d) {
                throw httpResponseException;
            }
        }
        return Collections.emptyList();
    }

    public List<String> getErrorDescriptions() {
        if (error != null) {
            return error.stream()
                    .map(err -> err.getErrorDescription())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public List<ErrorEntity> getError() {
        return error;
    }

    @JsonIgnore
    public Optional<String> getErrorDescription() {
        return Optional.ofNullable(error)
                .map(
                        errorList ->
                                errorList.stream()
                                        .findFirst()
                                        .map(ErrorEntity::getErrorDescription))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public static class ErrorDescriptions {

        public static final String DEVICE_ALREAY_EXISTS =
                "A device already exists for the current installation.";
        public static final String CALLBACK_URL_ALREADY_REGISTERED =
                "The Callback URL has already been registered for this Client.";
        public static final String ONLY_ONE_OAUTH_REGISTERED =
                "You can only have one active OAuth Client registered.";
    }
}
