package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

/** Utility for building and signing jwt. */
public final class TinkJwt {

    private final Map<String, Object> payloadClaims;
    private final Map<String, Object> headerClaims;

    private TinkJwt() {
        this.payloadClaims = new HashMap<>();
        this.headerClaims = new HashMap<>();
    }

    public static TinkJwt create() {
        return new TinkJwt();
    }

    /**
     * Add specific Claims to set as the Header. Method preserves already present claims.
     *
     * @param headerClaims the values to use as Claims in the token's Header.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withHeader(Map<String, Object> headerClaims) {
        this.headerClaims.putAll(headerClaims);
        return this;
    }

    /**
     * Add specific Claims to set as the Payload. Method preserves already present claims.
     *
     * @param payloadClaims the values to use as Claims in the token's Header.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withPayload(Map<String, Object> payloadClaims) {
        this.payloadClaims.putAll(payloadClaims);
        return this;
    }

    /**
     * Add a specific Key Id ("kid") claim to the Header. If the {@link Algorithm} used to sign this
     * token was instantiated with a KeyProvider, the 'kid' value will be taken from that provider
     * and this one will be ignored.
     *
     * @param keyId the Key Id value.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withKeyId(String keyId) {
        this.headerClaims.put(JwtClaims.KEY_ID, keyId);
        return this;
    }

    /**
     * Adds array of strings under crit header.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7515#section-4.1.11">rfc7515 docs</a>
     * @param items references to the crit header keys.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withCriticalHeaders(String[] items) {
        this.headerClaims.put(JwtClaims.CRIT, items);
        return this;
    }

    /**
     * Add a specific Issuer ("iss") claim to the Payload.
     *
     * @param issuer the Issuer value.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withIssuer(String issuer) {
        addClaim(JwtClaims.ISSUER, issuer);
        return this;
    }

    /**
     * Add a specific Subject ("sub") claim to the Payload.
     *
     * @param subject the Subject value.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withSubject(String subject) {
        addClaim(JwtClaims.SUBJECT, subject);
        return this;
    }

    /**
     * Add a specific Audience ("aud") claim to the Payload.
     *
     * @param audience the Audience value.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withAudience(String... audience) {
        if (audience != null && audience.length == 1) {
            addClaim(JwtClaims.AUDIENCE, audience[0]);
        } else {
            addClaim(JwtClaims.AUDIENCE, audience);
        }
        return this;
    }

    /**
     * Add a specific Expires At ("exp") claim to the Payload.
     *
     * @param expiresAt the Expires At value.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withExpiresAt(Instant expiresAt) {
        addClaim(JwtClaims.EXPIRES_AT, expiresAt.getEpochSecond());
        return this;
    }

    /**
     * Add a specific Not Before ("nbf") claim to the Payload.
     *
     * @param notBefore the Not Before value.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withNotBefore(Instant notBefore) {
        addClaim(JwtClaims.NOT_BEFORE, notBefore.getEpochSecond());
        return this;
    }

    /**
     * Add a specific Issued At ("iat") claim to the Payload.
     *
     * @param issuedAt the Issued At value.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withIssuedAt(Instant issuedAt) {
        addClaim(JwtClaims.ISSUED_AT, issuedAt.getEpochSecond());
        return this;
    }

    /**
     * Add a specific JWT Id ("jti") claim to the Payload.
     *
     * @param jwtId the Token Id value.
     * @return this same TinkJwt instance.
     */
    public TinkJwt withJWTId(String jwtId) {
        addClaim(JwtClaims.JWT_ID, jwtId);
        return this;
    }

    /**
     * Add a custom Claim value.
     *
     * @param name the Claim's name.
     * @param value the Claim's value.
     * @return this same TinkJwt instance.
     * @throws IllegalArgumentException if the name is null.
     */
    public TinkJwt withClaim(String name, Boolean value) throws IllegalArgumentException {
        assertNonNull(name);
        addClaim(name, value);
        return this;
    }

    /**
     * Add a custom Claim value.
     *
     * @param name the Claim's name.
     * @param value the Claim's value.
     * @return this same TinkJwt instance.
     * @throws IllegalArgumentException if the name is null.
     */
    public TinkJwt withClaim(String name, Integer value) throws IllegalArgumentException {
        assertNonNull(name);
        addClaim(name, value);
        return this;
    }

    /**
     * Add a custom Claim value.
     *
     * @param name the Claim's name.
     * @param value the Claim's value.
     * @return this same TinkJwt instance.
     * @throws IllegalArgumentException if the name is null.
     */
    public TinkJwt withClaim(String name, Long value) throws IllegalArgumentException {
        assertNonNull(name);
        addClaim(name, value);
        return this;
    }

    /**
     * Add a custom Claim value.
     *
     * @param name the Claim's name.
     * @param value the Claim's value.
     * @return this same TinkJwt instance.
     * @throws IllegalArgumentException if the name is null.
     */
    public TinkJwt withClaim(String name, Double value) throws IllegalArgumentException {
        assertNonNull(name);
        addClaim(name, value);
        return this;
    }

    /**
     * Add a custom Claim value.
     *
     * @param name the Claim's name.
     * @param value the Claim's value.
     * @return this same TinkJwt instance.
     * @throws IllegalArgumentException if the name is null.
     */
    public TinkJwt withClaim(String name, String value) throws IllegalArgumentException {
        assertNonNull(name);
        addClaim(name, value);
        return this;
    }

    /**
     * Add a custom Claim value.
     *
     * @param name the Claim's name.
     * @param value the Claim's value.
     * @return this same TinkJwt instance.
     * @throws IllegalArgumentException if the name is null.
     */
    public TinkJwt withClaim(String name, Date value) throws IllegalArgumentException {
        assertNonNull(name);
        addClaim(name, value);
        return this;
    }

    /**
     * Add a custom Claim value.
     *
     * @param name the Claim's name.
     * @param value the Claim's value.
     * @return this same TinkJwt instance.
     * @throws IllegalArgumentException if the name is null.
     */
    public TinkJwt withClaim(String name, Object value) throws IllegalArgumentException {
        assertNonNull(name);
        addClaim(name, value);
        return this;
    }

    /**
     * Add a custom Array Claim with the given items.
     *
     * @param name the Claim's name.
     * @param items the Claim's value.
     * @return this same TinkJwt instance.
     * @throws IllegalArgumentException if the name is null.
     */
    public TinkJwt withArrayClaim(String name, String[] items) throws IllegalArgumentException {
        assertNonNull(name);
        addClaim(name, items);
        return this;
    }

    /**
     * Add a custom Array Claim with the given items.
     *
     * @param name the Claim's name.
     * @param items the Claim's value.
     * @return this same TinkJwt instance.
     * @throws IllegalArgumentException if the name is null.
     */
    public TinkJwt withArrayClaim(String name, Integer[] items) throws IllegalArgumentException {
        assertNonNull(name);
        addClaim(name, items);
        return this;
    }

    /**
     * Add a custom Array Claim with the given items.
     *
     * @param name the Claim's name.
     * @param items the Claim's value.
     * @return this same TinkJwt instance.
     * @throws IllegalArgumentException if the name is null.
     */
    public TinkJwt withArrayClaim(String name, Long[] items) throws IllegalArgumentException {
        assertNonNull(name);
        addClaim(name, items);
        return this;
    }

    /**
     * Creates a new JWT and signs is with the given algorithm
     *
     * @param signer used to sign the JWT
     * @return a new JWT token in format {header}.{payload}.{signature}
     * @throws IllegalArgumentException if the provided algorithm is null.
     */
    public String signAttached(JwtSigner.Algorithm algorithm, JwtSigner signer)
            throws IllegalArgumentException {
        if (signer == null) {
            throw new IllegalArgumentException("The Algorithm cannot be null.");
        }

        return signer.sign(algorithm, headerClaims, payloadClaims, false);
    }

    /**
     * Creates a new detached payload JWT and signs is with the given algorithm. Returning in format
     *
     * @param signer used to sign the JWT
     * @return a new JWT token in format {header}..{signature}
     * @throws IllegalArgumentException if the provided algorithm is null.
     */
    public String signDetached(JwtSigner.Algorithm algorithm, JwtSigner signer)
            throws IllegalArgumentException {
        if (signer == null) {
            throw new IllegalArgumentException("The Algorithm cannot be null.");
        }

        return signer.sign(algorithm, headerClaims, payloadClaims, true);
    }

    private void assertNonNull(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The Custom Claim's name can't be null.");
        }
    }

    private void addClaim(String name, Object value) {
        if (value == null) {
            payloadClaims.remove(name);
            return;
        }
        payloadClaims.put(name, value);
    }
}
