package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.unittest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountIdentifierEntity;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class SantanderAccountIdentifierTest {

    private String balanceJSON =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"307.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private String accountJSON =
            "{\"AccountId\":\"4cc\",\"Currency\":\"GBP\",\"AccountType\":\"Personal\",\"AccountSubType\":\"Savings\",\"Account\":[{\"SchemeName\":\"UK.Santander.SavingsRollNumber\",\"Identification\":\"SAVINGR11111111\"}],\"Servicer\":{\"SchemeName\":\"BICFI\",\"Identification\":\"AAAAAAAAAAA\"}}";
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testToTransactionalAccount() throws Exception {

        String partyName = "MISS UNKNOWN";
        AccountBalanceEntity balance = mapper.readValue(balanceJSON, AccountBalanceEntity.class);
        AccountEntity account = mapper.readValue(accountJSON, AccountEntity.class);

        String accountNumber = account.getUniqueIdentifier();
        String accountName = account.getDisplayName();
        String holder = account.getDefaultIdentifier().getOwnerName();

        HolderName holderName =
                Objects.nonNull(holder) ? new HolderName(holder) : new HolderName(partyName);

        /*
        TODO: We need to remove this ugly fix that has been done to make Revolut work without
        doing data migrations. uniqueIdentifier should always be accountNumber in ideal case.
         */

        Optional<String> revolutAccount =
                account.getIdentifiers().stream()
                        .filter(
                                e ->
                                        e.getIdentifierType()
                                                .equals(ExternalAccountIdentification4Code.IBAN))
                        .map(AccountIdentifierEntity::getIdentification)
                        .filter(x -> x.contains("REVO"))
                        .findAny();

        String uniqueIdentifier =
                revolutAccount.isPresent() ? account.getAccountId() : accountNumber;

        IdBuildStep idModuleBuilder =
                IdModule.builder()
                        .withUniqueIdentifier(uniqueIdentifier)
                        .withAccountNumber(accountNumber)
                        .withAccountName(accountName)
                        .addIdentifier(
                                account.toAccountIdentifier(accountName)
                                        .orElseThrow(
                                                () ->
                                                        new IllegalStateException(
                                                                "Unable to set identifier")));

        TransactionalAccount transactionalAccount =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.from(account.getAccountType()).get())
                        .withoutFlags()
                        .withBalance(BalanceModule.of(balance.calculateAccountSpecificBalance()))
                        .withId(idModuleBuilder.build())
                        .setApiIdentifier(account.getAccountId())
                        .addHolderName(holderName.toString())
                        .build()
                        .get();
    }
}
