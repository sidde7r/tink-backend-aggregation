package se.tink.backend.aggregation.agents.brokers.nordnet.model.html;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CompleteBankIdPage {
    private final Element form;

    public CompleteBankIdPage(String html) {
        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(html), "No html provided for the complete BankID page");

        Document doc = Jsoup.parse(html);
        Element form = doc.getElementById("responseForm");
        Preconditions.checkState(form != null, "ResponseForm not present");

        this.form = form;
    }

    public String getSamlResponse() {
        Element input = form.select("input[name=SAMLResponse]").first();
        Preconditions.checkState(input != null, "Couldn't find SAMLResponse input field");

        String value = input.attr("value");
        Preconditions.checkState(
                !Strings.isNullOrEmpty(value), "No SAMLResponse input value available");

        return value;
    }

    public String getTarget() {
        String target = form.attr("action");
        Preconditions.checkState(!Strings.isNullOrEmpty(target), "No form action available");

        return target;
    }
}
