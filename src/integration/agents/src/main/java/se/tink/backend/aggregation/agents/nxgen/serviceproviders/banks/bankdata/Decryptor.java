package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

public interface Decryptor {

    byte[] decrypt(final String data);
}
