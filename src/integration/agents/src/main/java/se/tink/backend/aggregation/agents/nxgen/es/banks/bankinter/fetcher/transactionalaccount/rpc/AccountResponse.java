package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.JsfPart;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.HtmlResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AccountResponse extends HtmlResponse {
    private static final Logger LOG = LoggerFactory.getLogger(AccountResponse.class);

    public AccountResponse(HttpResponse response) {
        super(response);
    }

    protected String getAccountName() {
        final String accountName = evaluateXPath("//span[@class='appTitle']", String.class);
        if (Strings.isNullOrEmpty(accountName)) {
            LOG.warn("Did not find account name.");
        }
        return accountName;
    }

    protected AccountIdentifier getAccountIdentifier() {
        final String ibanString =
                evaluateXPath("//div[@id='js-copy-md']//div[@class='cuenta']", String.class)
                        .replaceAll("\\s", "");
        AccountIdentifier identifier = AccountIdentifier.create(Type.IBAN, ibanString);
        identifier.setName(getAccountName());
        if (!identifier.isValid()) {
            throw new IllegalStateException("Found invalid account IBAN.");
        }
        return identifier;
    }

    protected ExactCurrencyAmount getBalance() {
        final String balanceString =
                evaluateXPath("//div[@class='saldoMov']//p[contains(@class,'cifra')]", String.class)
                        .replaceAll("\\s", "");
        if (Strings.isNullOrEmpty(balanceString)) {
            throw new IllegalStateException("Did not find account balance.");
        }

        return parseAmount(balanceString);
    }

    protected List<String> getHolderNames(JsfUpdateResponse accountInfo) {
        final Document accountDetails = accountInfo.getUpdateDocument(JsfPart.ACCOUNT_DETAILS);
        final NodeList holderNames =
                evaluateXPath(
                        accountDetails,
                        "//dt[text()='Titulares']/following-sibling::dd/p",
                        NodeList.class);
        ArrayList<String> namesList = new ArrayList<>();
        for (int i = 0; i < holderNames.getLength(); i++) {
            namesList.add(holderNames.item(i).getTextContent());
        }
        return namesList;
    }

    public TransactionalAccount toTinkAccount(int accountIndex, JsfUpdateResponse accountInfo) {
        final AccountIdentifier accountIdentifier = getAccountIdentifier();
        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withBalance(BalanceModule.of(getBalance()))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountIdentifier.getIdentifier())
                                        .withAccountNumber(accountIdentifier.getIdentifier())
                                        .withAccountName(getAccountName())
                                        .addIdentifier(accountIdentifier)
                                        .build())
                        .setApiIdentifier(Integer.toString(accountIndex));

        final List<String> holderNames = getHolderNames(accountInfo);
        for (String name : holderNames) {
            builder = builder.addHolderName(name);
        }

        return builder.build();
    }
}
