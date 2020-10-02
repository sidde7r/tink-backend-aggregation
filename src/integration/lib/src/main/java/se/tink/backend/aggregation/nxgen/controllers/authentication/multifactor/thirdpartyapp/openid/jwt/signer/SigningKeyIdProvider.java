package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer;

public interface SigningKeyIdProvider {

    /**
     * This method should return signing key id, e.g.
     * 319249827807263620565033445582138561170152977440
     *
     * @return signing key id
     */
    String get();
}
