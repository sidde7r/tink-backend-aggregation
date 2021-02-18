package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.identity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IdentityDataEntity {
    private String fullName;
    private String obfuscatedSsn;
}
