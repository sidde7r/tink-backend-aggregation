package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.LaBanquePostaleTransactionalAccountFetcher;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class AccountEntity {

    private static final Logger logger = LoggerFactory.getLogger(LaBanquePostaleTransactionalAccountFetcher.class);

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
    private UnknownHash71A6C34B1D40C8Fafe9A6D43279845AbEntity outstandingCardsNotArrested;
    @JsonProperty("encoursCartesArrete")
    private UnknownHash71A6C34B1D40C8Fafe9A6D43279845AbEntity outstandingCardsArrete;
    @JsonProperty("totalEncoursCartes")
    private double totalOutstandingCards;
    @JsonProperty("carte")
    private MapEntity map;

    public AccountTypes toTinkAccountType() {
        if (LaBanquePostaleConstants.AccountType._000001.equals(productRange)) {
            return AccountTypes.CHECKING;
        } else if (LaBanquePostaleConstants.AccountType._000002.equals(productRange)) {
            return AccountTypes.SAVINGS;
        } else {
            logger.info("{} Unknown account type: {}", LaBanquePostaleConstants.Logging.UNKNOWN_ACCOUNT_TYPE,
                    productRange);
            return AccountTypes.OTHER;
        }
    }

    public TransactionalAccount toTinkAccount() {

        TransactionalAccount.Builder builder = TransactionalAccount.builder(toTinkAccountType(), number, balance);

        builder.setAccountNumber(number);
        builder.setName(Strings.isNullOrEmpty(libelleCustomize) ? nature.getLabel() : libelleCustomize);
        builder.setBankIdentifier(number);

        return builder.build();
    }

}
