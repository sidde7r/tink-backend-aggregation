package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.utils.WizinkDecoder;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@Slf4j
@JsonObject
public class ProductEntity {
    @JsonProperty("interveners")
    private List<PartyEntity> holdersList;

    @JsonProperty("formattedKeyDisplay")
    private String encodedIban;

    private String alias;
    private String availableBalance;
    private String balance;
    private String currency;
    private String internalKey;
    private String product;
    private String productType;

    public TransactionalAccountType getAccountType() {
        if ("AV".equals(productType)) {
            return TransactionalAccountType.SAVINGS;
        }
        log.info("Unknown account type {} with alias {}", productType, alias);
        return null;
    }

    private String parseBalance() {
        return balance.replace(",", ".");
    }

    private String parseAvailableBalance() {
        return Optional.ofNullable(availableBalance).orElse(balance).replace(",", ".");
    }

    public Optional<TransactionalAccount> toTinkAccount(String xTokenUser) {
        String maskedIban = WizinkDecoder.decodeNumber(encodedIban, xTokenUser);

        return TransactionalAccount.nxBuilder()
                .withType(getAccountType())
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(ExactCurrencyAmount.of(parseBalance(), currency))
                                .setAvailableBalance(
                                        ExactCurrencyAmount.of(parseAvailableBalance(), currency))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(maskedIban)
                                .withAccountNumber(maskedIban)
                                .withAccountName(product)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.IBAN, maskedIban, product))
                                .build())
                .addParties(PartyEntity.toTinkParties(holdersList))
                .setApiIdentifier(internalKey)
                .setBankIdentifier(maskedIban)
                .build();
    }
}
