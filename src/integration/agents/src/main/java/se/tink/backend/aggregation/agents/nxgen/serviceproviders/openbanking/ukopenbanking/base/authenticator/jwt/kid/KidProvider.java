package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.kid;

public interface KidProvider {

    /**
     * This method should return kid id, e.g. 319249827807263620565033445582138561170152977440
     *
     * @return kid Id
     */
    String get();
}
