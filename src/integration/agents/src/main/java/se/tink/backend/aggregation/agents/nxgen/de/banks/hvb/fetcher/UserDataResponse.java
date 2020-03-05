package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
class UserDataResponse {
    @JsonProperty("userdata")
    private User user;

    @JsonProperty("response")
    List<ResponseDetails> details;

    @Data
    @JsonObject
    static class User {
        private String ndg;
        private String surname;
        private String name;
    }

    @Data
    @JsonObject
    static class ResponseDetails {
        @JsonProperty("reb")
        private String directBankingNumber;
    }
}
