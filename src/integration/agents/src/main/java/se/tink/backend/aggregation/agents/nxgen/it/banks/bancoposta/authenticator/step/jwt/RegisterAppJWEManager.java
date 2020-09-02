package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.JWT.Claims;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;

@AllArgsConstructor
public class RegisterAppJWEManager {

    private UserContext userContext;

    public String genRegisterAppJWE() {
        return new JWE.Builder()
                .setJWEHeaderWithKeyId(userContext.getAppId())
                .setJwtClaimsSet(buildClaims())
                .setRSAEnrypter(userContext.getPubServerKey())
                .build();
    }

    private JWTClaimsSet buildClaims() {
        return new JWEClaims.Builder()
                .setDefaultValues()
                .setOtpSpecClaims(userContext.getOtpSecretKey(), userContext.getAppId())
                .setData(getDataClaims())
                .build();
    }

    private Map<String, String> getDataClaims() {
        Map<String, String> data = new HashMap<>();
        data.put("registerToken", userContext.getRegisterToken());
        data.put("idpAccessToken", userContext.getRegistrationSessionToken());
        if (userContext.isUserPinSetRequired()) {
            data.put(Claims.USER_PIN, userContext.getUserPin());
        }
        return data;
    }
}
