package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AreaEntity;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

@Slf4j
public class CreditAgricoleHolderNameExtractor {

    private static final List<String> COURTESY_TITLES =
            Lists.newArrayList(
                    "M.OU MME ",
                    "MELLE ",
                    "MLLE ",
                    "MLE ",
                    "MME ",
                    "MIN/ADM.",
                    "MONSIEUR ",
                    "MADAME ",
                    "LE ",
                    "M ",
                    "M. ",
                    "MR ");
    private static final String OR_SEPARATOR = " OU ";

    private static final Set<String> OMITTED_VALUES =
            Sets.newHashSet("Compte de paiement", "ASSOC DYNAMIT AGE");

    private static final Pattern NAME_SURNAME_PATTERN = Pattern.compile("([A-Z]){2,} ([A-Z]){2,}");

    /**
     * contains information about account holder. The structure of this value is usually sth like:
     * M. Name Surname. Sometimes multiple holders are available in retrieved value. Moreover,
     * sometimes only name and surname is passed.
     *
     * @return List of holder names
     */
    public static List<Party> extractAccountHolders(String name, AccountIdEntity accountId) {
        List<String> holderNames = new ArrayList<>(extractFromName(name));

        if (holderNames.isEmpty()) {
            holderNames.addAll(retrieveFromAccountId(accountId));
            log.warn("Unknown format of holder name value!");
        }

        return holderNames.stream()
                .map(holderName -> new Party(holderName, Party.Role.HOLDER))
                .collect(Collectors.toList());
    }

    private static List<String> extractFromName(String name) {
        if (OMITTED_VALUES.contains(name)) {
            return Collections.emptyList();
        } else if (NAME_SURNAME_PATTERN.matcher(name).matches()) {
            return Collections.singletonList(name);
        } else {
            return extractValues(name);
        }
    }

    private static List<String> extractValues(String name) {
        return splitMultipleAccountHolders(name).orElse(Lists.newArrayList(name)).stream()
                .map(CreditAgricoleHolderNameExtractor::removeCourtesyTittle)
                .collect(Collectors.toList());
    }

    private static Optional<ArrayList<String>> splitMultipleAccountHolders(String name) {
        int index = name.indexOf(OR_SEPARATOR);
        if (index == -1) {
            return Optional.empty();
        }
        String holderName1 = name.substring(0, index);
        String holderName2 = name.substring(index + OR_SEPARATOR.length());
        return Optional.of(Lists.newArrayList(holderName1, holderName2));
    }

    private static String removeCourtesyTittle(String name) {
        return COURTESY_TITLES.stream()
                .filter(name::contains)
                .map(title -> name.substring(name.indexOf(title) + title.length()))
                .findFirst()
                .orElse(name);
    }

    private static List<String> retrieveFromAccountId(AccountIdEntity accountId) {
        return Optional.ofNullable(accountId)
                .map(CreditAgricoleHolderNameExtractor::mapAccountId)
                .orElse(Collections.emptyList());
    }

    private static List<String> mapAccountId(AccountIdEntity accountId) {
        return Optional.ofNullable(accountId.getArea())
                .map(CreditAgricoleHolderNameExtractor::mapAreaId)
                .orElse(Collections.emptyList());
    }

    private static List<String> mapAreaId(AreaEntity areaEntity) {
        return extractFromName(areaEntity.getAreaLabel());
    }
}
