package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.jsoup.nodes.Element;

public class SignFormRequestBody extends MultivaluedMapImpl {
    @JsonIgnore
    private String signUri;
    @JsonIgnore
    private String referer;

    public String getClient() {
        return getFirst("nx_client");
    }

    private void setTbs(String template) {
        if (template == null) {
            return;
        }

        add("nx_tbs", template.replace("\n", "\r\n"));
    }

    public void setTokensAndNonVisible(String strutsTokenName, String token) {
        String nonVisible = "Empty tbs";

        if (!Strings.isNullOrEmpty(strutsTokenName) && !Strings.isNullOrEmpty(token)) {
            add("struts.token.name", strutsTokenName);
            add("token", token);
            nonVisible = "struts.token.name=" + strutsTokenName + "&token=" + token;
        }

        add("nx_nonvisible", nonVisible);
    }

    public String getSignUri() {
        return signUri;
    }

    public void setSignUri(String signUrl) {
        this.signUri = signUrl;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    @Override
    public SignFormRequestBody clone() {
        return (SignFormRequestBody) super.clone();
    }

    public static SignFormRequestBody from(Element signForm) {
        SignFormRequestBody signFormRequestBody = new SignFormRequestBody();

        for (Element input : signForm.select("input")) {
            if (Strings.nullToEmpty(input.attr("name")).startsWith("nx_")) {
                signFormRequestBody.add(input.attr("name"), input.val());
            }
        }

        String strutsTokenName = getStrutsTokenNameFrom(signForm);
        String token = getTokenFrom(signForm);

        signFormRequestBody.setTokensAndNonVisible(strutsTokenName, token);
        signFormRequestBody.setTbs(signFormRequestBody.getFirst("nx_template"));
        signFormRequestBody.setSignUri(signForm.attr("action"));

        return signFormRequestBody;
    }

    private static String getStrutsTokenNameFrom(Element signForm) {
        Element input = signForm.select("input[name=struts.token.name]").first();
        return input != null ? input.val() : null;
    }

    private static String getTokenFrom(Element signForm) {
        Element input = signForm.select("input[name=token]").first();
        return input != null ? input.val() : null;
    }
}
