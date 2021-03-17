package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import java.util.Arrays;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrustAnchorDomainParser {

    static String getTrustAnchorDomain(SoftwareStatementAssertion softwareStatement) {
        Objects.requireNonNull(softwareStatement.getJwksEndpoint());
        final String jwksHost = softwareStatement.getJwksEndpoint().toUri().getHost();

        final String[] subdomains = jwksHost.split("\\.");
        final int length = subdomains.length;

        return length < 4
                ? jwksHost
                : String.join(".", Arrays.copyOfRange(subdomains, length - 3, length));
    }
}
