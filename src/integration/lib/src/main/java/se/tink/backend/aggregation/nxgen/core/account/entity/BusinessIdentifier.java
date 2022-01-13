package se.tink.backend.aggregation.nxgen.core.account.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class BusinessIdentifier {
    @ToString.Include private BusinessIdentifierType type;
    private String value;
}
