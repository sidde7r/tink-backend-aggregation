package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator;

import com.google.common.base.Preconditions;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class NordeaPartnerJweHelper {

    private final NordeaPartnerKeystore keystore;
    private final NordeaPartnerConfiguration configuration;

    public NordeaPartnerJweHelper(
            NordeaPartnerKeystore keystore, NordeaPartnerConfiguration configuration) {
        this.keystore = keystore;
        this.configuration = configuration;
    }

    public OAuth2Token createToken(String partnerUid) {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(partnerUid), "Partner User Id can not be null or empty");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiresAt =
                LocalDateTime.now().plusSeconds(NordeaPartnerConstants.Jwt.TOKEN_LIFETIME_SECONDS);

        // throw exception if partner user id is not stored
        JWTClaimsSet claims =
                new JWTClaimsSet.Builder()
                        .issuer(NordeaPartnerConstants.Jwt.ISSUER)
                        .subject(partnerUid)
                        .expirationTime(toDate(accessExpiresAt))
                        .notBeforeTime(toDate(now))
                        .issueTime(toDate(now))
                        .jwtID(UUID.randomUUID().toString())
                        .build();

        JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);

        SignedJWT signedJWT = new SignedJWT(header, claims);

        try {

            signedJWT.sign(new RSASSASigner(keystore.getTinkSigningKey()));
            JWEObject jweObject =
                    new JWEObject(
                            new JWEHeader.Builder(
                                            JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM)
                                    .contentType(NordeaPartnerConstants.Jwt.JWT_CONTENT_TYPE)
                                    .keyID(configuration.getKeyId())
                                    .build(),
                            new Payload(signedJWT));
            jweObject.encrypt(new RSAEncrypter(keystore.getNordeaEncryptionPublicKey()));
            String serialized = jweObject.serialize();
            return OAuth2Token.createBearer(
                    serialized,
                    null,
                    Duration.between(LocalDateTime.now(), accessExpiresAt).getSeconds());
        } catch (JOSEException e) {
            throw new IllegalStateException("couldn't create JWT access token", e);
        }
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
