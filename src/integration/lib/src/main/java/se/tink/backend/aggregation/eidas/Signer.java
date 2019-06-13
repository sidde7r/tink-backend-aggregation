package se.tink.backend.aggregation.eidas;

/** Abstraction for any class that can create a signature from a signing string. */
public interface Signer {
    byte[] getSignature(byte[] signingBytes);
}
