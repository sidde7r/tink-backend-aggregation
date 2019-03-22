package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

public class IngAtRSAPublicKeyParser {
    private Document doc;

    public IngAtRSAPublicKeyParser(Document doc) {
        this.doc = doc;
    }

    public IngAtRSAPublicKeyParser(String htmlText) {
        this(Jsoup.parse(htmlText));
    }

    public Document getDocument() {
        return doc;
    }

    private BigInteger getAssignedValue(String s, String variableName) {
        Pattern p = Pattern.compile("var\\s+" + variableName + "\\s*=\\s*['\"]([0-9a-fA-F]+)['\"]");
        Matcher m = p.matcher(s);
        if (m.find()) {
            return new BigInteger(m.group(1), 16);
        }
        throw new IllegalStateException("Failed to extract the value for \"" + variableName + "\"");
    }

    // var mod = 'c61f....'
    private BigInteger getModulus(String s) {
        return getAssignedValue(s, "mod");
    }

    // var pubExp = '10001';
    private BigInteger getPublicExponent(String s) {
        return getAssignedValue(s, "pubExp");
    }

    private RSAPublicKeySpec parseJavaScript(String javaScriptCode) {
        return new RSAPublicKeySpec(getModulus(javaScriptCode), getPublicExponent(javaScriptCode));
    }

    public Optional<RSAPublicKeySpec> getPublicKeySpec() {
        for (Element e : doc.select("script")) {
            Attributes attrs = e.attributes();
            if (attrs.get("type").equalsIgnoreCase("text/javascript") && !attrs.hasKey("src")) {
                Node c = e.childNode(0);
                String data = c.attributes().get("data");
                if (data.contains("rsa_encrypt")) {
                    RSAPublicKeySpec encryptionInfo = parseJavaScript(data);
                    return Optional.of(encryptionInfo);
                }
            }
        }
        return Optional.empty();
    }

    public static RSAPublicKeySpec getDefaultRSAPublicKeySpec() {
        return new RSAPublicKeySpec(
                new BigInteger("10001", 16),
                new BigInteger(
                        "c61fd27bbc76f6ba778a9985683f876c86f30ee1200d584d6cc2f3af2682f9d2e6658d2686a0278f8c5a3fb2ad8c168b4b0059411022a45520ebb80a7e9b215600b65494b2d3639a354b1eedb4ac068cc333027a99cf0874a18c0f4d3e49a355e2948e3b5fb2968e4c49230b73bd7f0a5e435f3bb4dd7ef6441b43194305972d",
                        16));
    }
}
