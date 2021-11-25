package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountHolder {
    private String accountId;
    private AccountHolderType type;
    private List<AccountParty> identities;
}
