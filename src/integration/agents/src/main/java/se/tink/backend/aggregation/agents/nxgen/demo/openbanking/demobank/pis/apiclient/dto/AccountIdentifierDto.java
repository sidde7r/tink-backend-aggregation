package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.libraries.account.enums.AccountIdentifierType;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AccountIdentifierDto {

    private String accountId;

    private AccountIdentifierType type;

    private String identifier;
}
