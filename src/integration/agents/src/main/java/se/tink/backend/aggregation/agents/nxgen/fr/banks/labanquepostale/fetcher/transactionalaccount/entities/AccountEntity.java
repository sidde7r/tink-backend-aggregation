package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.LaBanquePostaleTransactionalAccountFetcher;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.OtherIdentifier;

@JsonObject
public class AccountEntity {

    private static final Logger logger =
            LoggerFactory.getLogger(LaBanquePostaleTransactionalAccountFetcher.class);

    @JsonProperty("codeEtablissement")
    private String establishmentCode;

    @JsonProperty("codeGuichet")
    private String guichetCode;

    @JsonProperty("numero")
    private String number;

    @JsonProperty("clefRIB")
    private String clefrib;

    @JsonProperty("intitule")
    private String entitled;

    @JsonProperty("libellePersonnalise")
    private String libelleCustomize;

    @JsonProperty("codeVarianteProduit")
    private String productVariantCode;

    @JsonProperty("gammeProduit")
    private String productRange;

    private String roleClient;
    private NatureEntity nature;

    @JsonProperty("soldeComptable")
    private AccountantBalanceEntity balance;

    @JsonProperty("avoirTechnique")
    private double haveTechnical;

    private String codeMessageReleve;

    @JsonProperty("numeroCompteAssocie")
    private String associateAccountNumber;

    @JsonProperty("typePartenaire")
    private String partnerType;

    @JsonProperty("encoursCartesNonArrete")
    private OutstandingCardsEntity outstandingCardsNotArrested;

    @JsonProperty("encoursCartesArrete")
    private OutstandingCardsEntity outstandingCardsArrete;

    @JsonProperty("totalEncoursCartes")
    private double totalOutstandingCards;

    @JsonProperty("carte")
    private MapEntity map;

    public TransactionalAccountType toTinkAccountType() {

        Optional<TransactionalAccountType> type =
                LaBanquePostaleConstants.AccountType.translate(productRange);

        return type.orElseGet(
                () -> {
                    logger.info(
                            "{} Unknown account type: {}",
                            LaBanquePostaleConstants.Logging.UNKNOWN_ACCOUNT_TYPE,
                            productRange);
                    return TransactionalAccountType.OTHER;
                });
    }

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(toTinkAccountType())
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(balance))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(number)
                                .withAccountNumber(number)
                                .withAccountName(
                                        Strings.isNullOrEmpty(libelleCustomize)
                                                ? nature.getLabel()
                                                : libelleCustomize)
                                .addIdentifier(new OtherIdentifier(number))
                                .build())
                .setApiIdentifier(number)
                .build();
    }
}
