package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity;

public class EncryptionValuesEntity {

    private String salt;
    private String modulus;
    private String exponent;

    public EncryptionValuesEntity(String salt, String modulus, String exponent) {
        this.salt = salt;
        this.modulus = modulus;
        this.exponent = exponent;
    }

    public String getSalt() {
        return salt;
    }

    public String getModulus() {
        return modulus;
    }

    public String getExponent() {
        return exponent;
    }
}
