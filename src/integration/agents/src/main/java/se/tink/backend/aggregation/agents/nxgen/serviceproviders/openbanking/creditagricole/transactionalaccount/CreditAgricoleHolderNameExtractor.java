package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreditAgricoleHolderNameExtractor {

    private static final List<String> COURTESY_TITLES =
            Lists.newArrayList(
                    "M.OU MME ", "M. ", "MLE ", "MME ", "MIN/ADM.", "MONSIEUR ", "MADAME ");
    private static final String OR_SEPARATOR = " OU ";

    /**
     * contains information about account holder. The structure of this value is usually sth like:
     * M. Name Surname. Sometimes multiple holders are available in retrieved value
     *
     * @return List of holder names
     */
    public static List<String> extract(String name) {
        for (String title : COURTESY_TITLES) {
            if (name.contains(title)) {
                String holderName = name.substring(name.indexOf(title) + title.length());
                return checkForSecondHolder(holderName);
            }
        }
        if (name.contains(OR_SEPARATOR)) {
            return checkForHolderNamesWithoutTitles(name);
        }
        log.warn("Unknown format of holder name value!");
        return Lists.newArrayList();
    }

    private static List<String> checkForSecondHolder(String holderName) {
        List<String> holderNames = Lists.newArrayList();
        if (holderName.contains(OR_SEPARATOR)) {
            int indexOf = holderName.indexOf(OR_SEPARATOR);
            String holderName2 = holderName.substring(indexOf + OR_SEPARATOR.length());
            List<String> extracted = extract(holderName2);
            holderName = holderName.substring(0, indexOf);
            holderNames.add(holderName);
            holderNames.addAll(extracted);
            return holderNames;
        }
        holderNames.add(holderName);
        return holderNames;
    }

    /**
     * Sometimes we can get name in format: holderName1 OU holderName2
     *
     * @return extracted values or empty list
     */
    private static List<String> checkForHolderNamesWithoutTitles(String name) {
        int index = name.indexOf(OR_SEPARATOR);
        if (index == -1) {
            return Lists.newArrayList();
        }
        String holderName1 = name.substring(0, index);
        String holderName2 = name.substring(index + OR_SEPARATOR.length());
        return Lists.newArrayList(holderName1, holderName2);
    }
}
