package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.entities.RefundSummaryEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class UserSummaryResponse {
    @JsonProperty("Accounts")
    private List<AccountEntity> accounts;

    @JsonProperty("ConsumerSocietyMemberships")
    private List<String> consumerSocietyMemberships;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("HasFinancialAccount")
    private boolean hasFinancialAccount;

    @JsonProperty("HouseholdMembers")
    private List<String> householdMembers;

    @JsonProperty("Id360")
    private String id360;

    @JsonProperty("IsEcommerceCustomer")
    private boolean isEcommerceCustomer;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("MedmeraId")
    private int medmeraId;

    @JsonProperty("MemberAccountType")
    private int memberAccountType;

    @JsonProperty("RefundSummary")
    private RefundSummaryEntity refundSummary;

    public Collection<CreditCardAccount> toTinkCards(String credentialsId) {
        return Optional.ofNullable(accounts).orElseGet(Collections::emptyList).stream()
                .filter(AccountEntity::isCard)
                .map(a -> a.toTinkCard(credentialsId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Collection<TransactionalAccount> toTinkAccounts(String credentialsId) {
        return Optional.ofNullable(accounts).orElseGet(Collections::emptyList).stream()
                .filter(a -> !a.isCard())
                .map(a -> a.toTinkAccount(credentialsId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
