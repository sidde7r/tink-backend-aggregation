package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

public class VolksbankCheckingAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private static final Pattern ACCOUNT_INFO =
            Pattern.compile(
                    "<.*Produktbezeichnung:[\\s\\S]*?>[\\s\\S]*?>(?<productName>.*(?=<)|.*(?=\\s*<))[\\s\\S]*?<.*\"iban\"[\\s\\S]*?>[\\s\\S]*?>(?<iban>.*(?=<)|.*(?=\\s*<))[\\s\\S]*?<.*Aktueller Kontostand[\\s\\S]*?value\">(?<amount>.*(?=<)|.*(?=\\s*<))[\\s\\S]*?unit\">(?<currency>.*(?=<)|.*(?=\\s*<))");

    private static final Pattern PRODUCT_ID =
            Pattern.compile(
                    "startseite-repeated-item-2680441:widget:form:produkte:produkteTable:table_data[\\s\\S]*?data-ri=\"(.*?(?=\")|.*(?=\\s*\"))");
    private final VolksbankApiClient apiClient;
    private final SessionStorage sessionStorage;

    public VolksbankCheckingAccountFetcher(
            VolksbankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        List<TransactionalAccount> accounts = new ArrayList<>();

        // TODO We don't have credentials that contain other account types, don't know yet how to
        // parse that
        // TODO We don't have credentials that contain more than one account.
        String body = apiClient.postMain().getBody(String.class);

        Matcher matcher = ACCOUNT_INFO.matcher(body);

        while (matcher.find()) {
            String iban = matcher.group(VolksbankConstants.REGEX_GROUP.IBAN).replace(" ", "");
            TransactionalAccount account =
                    TransactionalAccount.builder(
                                    AccountTypes.CHECKING,
                                    iban,
                                    new Amount(
                                            matcher.group(VolksbankConstants.REGEX_GROUP.CURRENCY),
                                            StringUtils.parseAmount(
                                                    matcher.group(
                                                            VolksbankConstants.REGEX_GROUP
                                                                    .AMOUNT))))
                            .setAccountNumber(iban)
                            .setName(matcher.group(VolksbankConstants.REGEX_GROUP.PRODUCT_NAME))
                            .addIdentifier(
                                    AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                            .build();
            accounts.add(account);
        }

        // NOTE try to cache all the product Ids for later usage
        matcher = PRODUCT_ID.matcher(body);
        List<String> providerIds = new ArrayList<>();
        while (matcher.find()) {
            providerIds.add(matcher.group(1));
        }
        if (providerIds.isEmpty()) {
            throw new IllegalStateException("No product Id found");
        }

        sessionStorage.put(VolksbankConstants.Storage.PRODUCT_ID, providerIds);

        return accounts.isEmpty() ? Collections.emptyList() : accounts;
    }
}
