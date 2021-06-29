package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.converter;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@Slf4j
public abstract class CmcicAccountBaseConverter<T extends Account> {

    private static final List<String> KNOWN_ACCOUNT_PREFIXES =
            Lists.newArrayList(
                    "START Jeunes Actifs",
                    "Compte Courant JEUNE ACTIF",
                    "COMPTE COURANT PRIVE EUR",
                    "COMPTE COURANT",
                    "C/C CONTRAT PERSONNEL GLOBAL",
                    "C/C CONTRAT PERSONNEL PARCOURS J",
                    "C/C EUROCOMPTE JEUNE",
                    "C/C EUROCOMPTE CONFORT",
                    "COMPTE CHEQUE EUROCOMPTE",
                    "COMPTE GO",
                    "COMPTE DE",
                    "C/C VIP+",
                    "C/C VIP");

    public abstract Optional<T> convertToAccount(AccountResourceDto accountResourceDto);

    /**
     * {@link AccountResourceDto#getName()} contains information about account holder & account
     * name. The structure of this value is usually sth like: 00000 000000000 00 C/C EUROCOMPTE
     * JEUNE M NAME SURNAME. This method formats this value by selecting a proper part.
     *
     * @return Formatted object containing both account name and holder name
     * @param accountResource
     */
    protected CmcicAccountNameAndHolderName getAccountNameAndHolderName(
            AccountResourceDto accountResource) {
        String resourceName = accountResource.getName();
        for (String accountPrefix : KNOWN_ACCOUNT_PREFIXES) {
            if (resourceName.contains(accountPrefix)) {
                return CmcicAccountNameAndHolderName.builder()
                        .accountName(accountPrefix)
                        .holderName(extractHolderName(resourceName, accountPrefix))
                        .build();
            }
        }
        log.warn("Unknown account prefix for CmcicAgent provider");
        return CmcicAccountNameAndHolderName.builder()
                .accountName(resourceName)
                .holderName("")
                .build();
    }

    protected IdModule getIdModule(AccountResourceDto accountResourceDto, String accountName) {
        String iban = accountResourceDto.getAccountId().getIban();
        return IdModule.builder()
                .withUniqueIdentifier(iban)
                .withAccountNumber(iban)
                .withAccountName(accountName)
                .addIdentifier(new IbanIdentifier(iban))
                .setProductName(accountResourceDto.getName())
                .build();
    }

    /**
     * Passed value contains part of iban number at the beginning of {@param resourceName} and it
     * has to be removed in the first place. After that passed account prefix: {@param
     * accountPrefix} has to removed to leave only a part with a holder name.
     *
     * @return Extracted value of the holder name
     * @param resourceName
     * @param accountPrefix
     */
    private String extractHolderName(String resourceName, String accountPrefix) {
        resourceName = resourceName.substring(resourceName.indexOf(accountPrefix));
        return resourceName.replace(accountPrefix + " ", "");
    }
}
