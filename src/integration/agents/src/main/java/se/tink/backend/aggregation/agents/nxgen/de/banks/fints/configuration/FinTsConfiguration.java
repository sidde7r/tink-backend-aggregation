package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FinTsConfiguration {
    private final String blz;
    private final Bank bank;
    private final String endpoint;
    private final String username;
    private final String password;
}
