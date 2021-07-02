package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.strings.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class AccountEntityMarketMapper {

    private static final int ACCOUNT_NO_MIN_LENGTH_DK = 10;

    private final String market;

    String getUniqueIdentifier(AccountEntity accountEntity) {
        String accountNumber = accountEntity.getAccountNoExt();
        switch (market) {
            case "DK":
                return getUniqueIdentifierDk(accountNumber);
            case "NO":
            case "SE":
            case "FI":
                return accountNumber;
            default:
                throw new IllegalStateException(
                        "Cannot prepare unique identifier for market: " + market);
        }
    }

    private String getUniqueIdentifierDk(String accountNumber) {
        if (accountNumber.length() < ACCOUNT_NO_MIN_LENGTH_DK) {
            log.warn(
                    "Account number is shorter than expected({}). Its length is [{}]",
                    ACCOUNT_NO_MIN_LENGTH_DK,
                    accountNumber.length());
        }
        return Strings.padStart(accountNumber, ACCOUNT_NO_MIN_LENGTH_DK, '0');
    }

    List<String> getAccountOwners(AccountDetailsResponse accountDetailsResponse) {
        switch (market) {
            case "DK":
                return AccountEntityMapperUtils.getNotBlankAccountOwners(accountDetailsResponse);
            case "NO":
            case "SE":
                return AccountEntityMapperUtils.extractAccountOwnersUsingPattern(
                        accountDetailsResponse);
            case "FI":
                // We don't know what is returned in Finland
                return Collections.emptyList();
            default:
                throw new IllegalStateException(
                        "Cannot prepare unique identifier for market: " + market);
        }
    }

    List<AccountIdentifier> getAccountIdentifiers(
            AccountEntity accountEntity, AccountDetailsResponse accountDetailsResponse) {
        List<AccountIdentifier> identifiers = new ArrayList<>();

        makeIdentifierAValidUriHost(accountDetailsResponse.getIban())
                .ifPresent(iban -> identifiers.add(new IbanIdentifier(iban)));

        makeIdentifierAValidUriHost(accountEntity.getAccountNoExt())
                .ifPresent(accountNo -> identifiers.add(getMarketSpecificIdentifier(accountNo)));
        return identifiers;
    }

    private static Optional<String> makeIdentifierAValidUriHost(String identifierValue) {
        return Optional.ofNullable(identifierValue)
                .map(StringUtils::removeNonAlphaNumeric)
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank);
    }

    private AccountIdentifier getMarketSpecificIdentifier(String accountNoExt) {
        switch (market) {
            case "DK":
                return new DanishIdentifier(accountNoExt);
            case "NO":
                return new BbanIdentifier(accountNoExt);
            case "SE":
                return new SwedishIdentifier(accountNoExt);
            case "FI":
                return new FinnishIdentifier(accountNoExt);
            default:
                throw new IllegalStateException(
                        "Cannot prepare account identifier for market: " + market);
        }
    }
}
