package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Accessors(chain = true)
@JsonObject
public class AccountsResponse {

    private Response response;

    @Data
    @Accessors(chain = true)
    @JsonObject
    static class Response {
        private List<Account> accounts;

        @Data
        @Accessors(chain = true)
        @JsonObject
        static class Account {
            private String id;

            @JsonProperty("nickname")
            private String name;

            private BigDecimal balance;
            private String currency;
            private String type;
            private Iban ibanInfo;
            private String branch;
            private LoginInfo loginInfo;

            @Data
            @Accessors(chain = true)
            @JsonObject
            static class Iban {
                private String iban;
            }

            String getHolderName() {
                return Optional.ofNullable(getLoginInfo())
                        .map(
                                login ->
                                        format(
                                                "%s %s",
                                                login.getHolderName(), login.getHolderSurname()))
                        .orElse("");
            }

            String getIban() {
                return Optional.ofNullable(getIbanInfo())
                        .map(Iban::getIban)
                        .orElseThrow(() -> new IllegalArgumentException("Can't obtain IBAN."));
            }

            @Data
            @Accessors(chain = true)
            @JsonObject
            static class LoginInfo {

                private String holderName;
                private String holderSurname;
            }
        }
    }
}
