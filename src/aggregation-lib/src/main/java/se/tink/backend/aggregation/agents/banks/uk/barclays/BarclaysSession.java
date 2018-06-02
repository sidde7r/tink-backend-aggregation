package se.tink.backend.aggregation.agents.banks.uk.barclays;

public class BarclaysSession {
    // All keys are derived from the seed.
    // The seed is either:
    // a) RSA encrypted and sent to the server so that the server can derive the same set of keys; OR
    // b) derived using ECDH (which is used in the final step of auth).
    // seed is 32 random bytes
    private byte[] seed;

    private byte[] cliAesKey;
    private byte[] srvAesKey;
    private byte[] cliIvKey;
    private byte[] srvIvKey;

    // The `keyConfirmationMsg` serves as a proof that we as a client has derived the same session keys
    // as the server.
    private byte[] keyConfirmationMsg;

    // Initial value is `0000000000000000`.
    // Server will give us one once we've performed the session key setup.
    private String sessionId = BarclaysConstants.DEFAULT_SESSION_ID;

    // The session is authenticated once we have performed step (b)
    private boolean authenticated = false;

    public BarclaysSession() {
        // Default session which assumes option (a)
        this(BarclaysCrypto.random(32));
    }

    public BarclaysSession(byte[] seed) {
        this(seed, BarclaysConstants.RSA_PUB_KEY);
    }

    public BarclaysSession(byte[] seed, byte[] seedIv) {
        setSeed(seed, seedIv);
    }

    public void setSeed(byte[] seed, byte[] seedIv) {
        this.seed = seed;
        byte[] internalKey = BarclaysCrypto.sha256(seed, seedIv);
        cliAesKey = BarclaysCrypto.aesEcbEncrypt(internalKey, BarclaysConstants.KEY_GEN_NONCE);
        srvAesKey = BarclaysCrypto.aesEcbEncrypt(internalKey, cliAesKey);
        cliIvKey = BarclaysCrypto.aesEcbEncrypt(internalKey, srvAesKey);
        srvIvKey = BarclaysCrypto.aesEcbEncrypt(internalKey, cliIvKey);

        // empty aad
        byte[] aad = new byte[0];
        keyConfirmationMsg = BarclaysCrypto.aesGcmEncrypt(
                                                    cliAesKey,
                                                    BarclaysConstants.KEY_GEN_AES_GCM_IV,
                                                    aad,
                                                    BarclaysConstants.KEY_GEN_NONCE);
    }

    public byte[] getSeed() {
        return seed;
    }

    public byte[] getCliAesKey() {
        return cliAesKey;
    }

    public byte[] getSrvAesKey() {
        return srvAesKey;
    }

    public byte[] getCliIvKey() {
        return cliIvKey;
    }

    public byte[] getSrvIvKey() {
        return srvIvKey;
    }

    public byte[] getKeyConfirmationMsg() {
        return keyConfirmationMsg;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean hasSessionId() {
        return !sessionId.equals(BarclaysConstants.DEFAULT_SESSION_ID);
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
