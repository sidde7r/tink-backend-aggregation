package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(onlyExplicitlyIncluded = true)
public class AccountParty {
    private String name;

    @JsonInclude(Include.NON_NULL)
    private String fullLegalName;

    @ToString.Include private HolderRole role;

    @JsonInclude(Include.NON_NULL)
    private List<BusinessIdentifier> businessIdentifiers;

    @JsonInclude(Include.NON_NULL)
    private List<AccountPartyAddress> addresses;
}
