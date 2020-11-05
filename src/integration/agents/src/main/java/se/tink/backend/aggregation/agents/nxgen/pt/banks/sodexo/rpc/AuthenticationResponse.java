package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AuthenticationResponse {
    @JsonProperty("session-token")
    private String sessionToken;

    @JsonProperty("user-token")
    private String userToken;

    @JsonProperty("card-number")
    private String cardNumber;

    @JsonProperty("card-status")
    private String cardStatus;

    @JsonProperty("first-name")
    private String firstName;

    private String surname;
    private String gender;
    private String email;
    private String phone;
    private String newsletter;

    @JsonProperty("birth-date")
    private Date birthDate;

    @JsonProperty("benefit-day")
    private int benefitDay;

    @JsonProperty("needs-consent")
    private String needsConsent;

    @JsonProperty("refeicoes-url")
    private String refeicoesUrl;
}
