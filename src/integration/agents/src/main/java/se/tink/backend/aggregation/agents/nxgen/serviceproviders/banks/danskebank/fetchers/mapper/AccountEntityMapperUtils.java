package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountEntityMapperUtils {

    // Single owner name example in SE: "ssn - fName lName"
    private static final Pattern EXTRACT_ACCOUNT_OWNERS_PATTERN = Pattern.compile("- (.*)");

    static List<String> getNotBlankAccountOwners(AccountDetailsResponse accountDetailsResponse) {
        return ListUtils.emptyIfNull(accountDetailsResponse.getAccountOwners()).stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    static List<String> extractAccountOwnersUsingPattern(
            AccountDetailsResponse accountDetailsResponse) {
        try {
            return ListUtils.emptyIfNull(accountDetailsResponse.getAccountOwners()).stream()
                    .filter(StringUtils::isNotBlank)
                    .map(AccountEntityMapperUtils::extractAccountOwner)
                    .collect(Collectors.toList());
        } catch (IllegalStateException e) {
            log.warn(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private static String extractAccountOwner(String accountOwner) {
        Matcher matcher = EXTRACT_ACCOUNT_OWNERS_PATTERN.matcher(accountOwner);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("Found accountOwner that couldn't be extracted");
    }
}
