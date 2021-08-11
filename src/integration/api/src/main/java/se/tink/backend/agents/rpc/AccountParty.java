package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

@Data
public class AccountParty {
    private String name;
    private HolderRole role;

    @JsonInclude(Include.NON_NULL)
    private List<AccountPartyAddress> addresses;
}
