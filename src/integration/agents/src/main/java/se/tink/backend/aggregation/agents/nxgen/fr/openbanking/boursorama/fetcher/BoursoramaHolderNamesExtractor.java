package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;

@Slf4j
public class BoursoramaHolderNamesExtractor {
    private static final List<String> REDUNDANT_PARTS =
            Lists.newArrayList("- ", "Carte Visa Classic ");
    private static final List<String> COURTESY_TITLES =
            Lists.newArrayList("M OU MME ", "MLLE ", "MME ", "MLE ", "MR ", "M ");

    private List<String> holderNames;

    public BoursoramaHolderNamesExtractor() {
        holderNames = new ArrayList<>();
    }

    /**
     * This method cuts courtesy titles from full name passed to it. If no of known titles is found
     * then empty will be returned.
     *
     * @return Optional of holder name
     */
    public List<Party> extract(String name) {
        name = removeRedundantParts(name);

        List<String> splitHolderNames = splitHolderNames(name);
        if (!splitHolderNames.isEmpty()) {
            splitHolderNames.forEach(v -> holderNames.add(removeCourtesyTitle(v)));
        } else {
            holderNames.add(removeCourtesyTitle(name));
        }

        List<Party> parties =
                holderNames.stream()
                        .map(holderName -> new Party(holderName, Role.HOLDER))
                        .collect(Collectors.toList());
        if (parties.isEmpty()) {
            log.warn("Unknown format of holder name value!");
        }
        return parties;
    }

    private String removeRedundantParts(String name) {
        for (String redundantPart : REDUNDANT_PARTS) {
            if (name.contains(redundantPart)) {
                name = name.replace(redundantPart, "");
            }
        }
        return name;
    }

    // In that situation we have to holder names (e.g. NAME1 SURNAME1 / NAME2 SURNAME2)
    private List<String> splitHolderNames(String name) {
        List<String> splittedValues = new ArrayList<>();
        int indexOfSeparator = name.indexOf("/");
        if (indexOfSeparator != -1) {
            splittedValues.add(name.substring(0, indexOfSeparator - 1));
            splittedValues.add(name.substring(indexOfSeparator + 2));
        }
        return splittedValues;
    }

    private String removeCourtesyTitle(String name) {
        for (String separator : COURTESY_TITLES) {
            if (name.contains(separator)) {
                int index = name.indexOf(separator);
                name = name.substring(index + separator.length());
            }
        }
        return name;
    }
}
