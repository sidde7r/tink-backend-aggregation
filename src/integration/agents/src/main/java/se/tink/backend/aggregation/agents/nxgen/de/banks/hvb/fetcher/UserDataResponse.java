package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Accessors(chain = true)
@JsonObject
class UserDataResponse {
    @JsonProperty("userdata")
    private User user;

    @JsonProperty("response")
    List<ResponseDetails> details;

    @Data
    @Accessors(chain = true)
    @JsonObject
    static class User {
        private String surname;
        private String name;
    }

    @Data
    @Accessors(chain = true)
    @JsonObject
    static class ResponseDetails {
        @JsonProperty("reb")
        private String directBankingNumber;

        @JsonProperty("rebDescr")
        private String directBankingOwner;
    }
}
