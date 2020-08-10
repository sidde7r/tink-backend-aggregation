package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UnicreditProviderConfiguration {
    private String psuIdType;
    private String baseUrl;
}
