package se.tink.backend.aggregation.agents.brokers.nordnet.model.html;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class LoginPage {
    private final Document doc;

    public LoginPage(String html) {
        doc = Jsoup.parse(html);
    }

    public String getUsernameFormKey() {
        String key = getLoginForm().getElementById("input1").attr("name");
        Preconditions.checkState(!Strings.isNullOrEmpty(key), "Couldn't find username form key");

        return key;
    }

    public String getPasswordFormKey() {
        String key = getLoginForm().getElementById("pContHidden").attr("name");
        Preconditions.checkState(!Strings.isNullOrEmpty(key), "Couldn't find password form key");

        return key;
    }

    private Element getLoginForm() {
        return doc.getElementById("loginForm");
    }
}
