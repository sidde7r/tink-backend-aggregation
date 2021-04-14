package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.rpc;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.entities.AccountEntity;

@Slf4j
public class GlobalPositionResponse {

    private @Getter @Setter Document html;

    public GlobalPositionResponse(String html) {
        this.html = Jsoup.parse(html);
    }

    public List<AccountEntity> getAccounts() {
        // Accounts reference
        List<AccountEntity> accountEntities = new ArrayList<>();

        Elements allAccounts = findAccountsElementOnHtml();

        // select the nodes that contains Account alias, Acc number, Balance
        for (Element accountContainer : allAccounts) {

            AccountEntity accountEntity = new AccountEntity();

            Element account = accountContainer.siblingElements().select("tr td.totlistaC").first();
            // .select("tr:contains(0198) td")

            Elements otherData =
                    accountContainer.siblingElements().select("tr td.totlistaC").nextAll();

            accountEntity.setAccountNumber(account.ownText().replaceAll("[\\s\\u202F\\u00A0]", ""));
            String alias = otherData.get(0).ownText();

            if (alias.contains("|")) {
                accountEntity.setAccountAlias(alias.split("\\|")[0].trim());
                accountEntity.setCurrency(checkAliasCurrency(alias));
            } else {
                accountEntity.setAccountAlias(alias);
            }

            accountEntity.setBalance(otherData.get(1).ownText());

            accountEntity.setForm(accountContainer);
            accountEntities.add(accountEntity);
        }

        return accountEntities;
    }

    public Elements findAccountsElementOnHtml() {
        return html.select("form[name*='form_a_']");
    }

    private String checkAliasCurrency(String alias) {

        String currency = alias.split("\\|")[1].trim().toUpperCase();
        if (currency.contains("€") || currency.contains("EUR") || currency.contains("&#8364;")) {
            currency = "EUR";
        }
        return currency;
    }
}
