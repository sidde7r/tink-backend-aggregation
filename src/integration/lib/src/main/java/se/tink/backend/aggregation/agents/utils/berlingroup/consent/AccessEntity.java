package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class AccessEntity {

    private List<AccountReferenceEntity> accounts;
    private List<AccountReferenceEntity> transactions;
    private List<AccountReferenceEntity> balances;

    private AccessType allPsd2;
    private AccessType availableAccountsWithBalances;
    private AccessType availableAccountsWithBalance;
    private AccessType availableAccounts;
}
