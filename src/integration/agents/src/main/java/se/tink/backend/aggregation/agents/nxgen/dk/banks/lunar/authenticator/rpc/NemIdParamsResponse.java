package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NemIdParamsResponse {

    private static final Pattern CHALLENGE_PATTERN = Pattern.compile("challenge=(.+)");

    @JsonProperty("CLIENTFLOW")
    private String clientflow;

    @JsonProperty("CLIENTMODE")
    private String clientmode;

    @JsonProperty("DIGEST_SIGNATURE")
    private String digestSignature;

    @JsonProperty("ENABLE_AWAITING_APP_APPROVAL_EVENT")
    private String enableAwaitingAppApprovalEvent;

    @JsonProperty("PARAMS_DIGEST")
    private String paramsDigest;

    @JsonProperty("SIGN_PROPERTIES")
    private String signProperties;

    @JsonProperty("SP_CERT")
    private String spCert;

    @JsonProperty("TIMESTAMP")
    private String timestamp;

    @JsonIgnore
    public String getChallenge() {
        if (StringUtils.isNotBlank(signProperties)) {
            Matcher matcher = CHALLENGE_PATTERN.matcher(signProperties);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        throw new IllegalArgumentException("Response does not contain challenge!");
    }
}
