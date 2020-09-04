package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.JWT.Claims;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;

@RequiredArgsConstructor
public class RegisterAppJWEManager {

    private final BancoPostaStorage storage;

    public String genRegisterAppJWE() {
        return new JWE.Builder()
                .setJWEHeaderWithKeyId(storage.getAppId())
                .setJwtClaimsSet(buildClaims())
                .setRSAEnrypter(storage.getPubServerKey())
                .build();
    }

    private JWTClaimsSet buildClaims() {
        return new JWEClaims.Builder()
                .setDefaultValues()
                .setOtpSpecClaims(storage.getOtpSecretKey(), storage.getAppId())
                .setData(getDataClaims())
                .build();
    }

    private Map<String, String> getDataClaims() {
        Map<String, String> data = new HashMap<>();
        data.put(Claims.REGISTER_TOKEN, storage.getRegisterToken());
        data.put(Claims.IDP_ACCESS_TOKEN, storage.getRegistrationSessionToken());
        if (storage.isUserPinSetRequired()) {
            data.put(Claims.USER_PIN, storage.getUserPin());
        }
        return data;
    }
}
