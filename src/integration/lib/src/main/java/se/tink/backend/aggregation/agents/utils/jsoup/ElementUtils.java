package se.tink.backend.aggregation.agents.utils.jsoup;

import com.google.common.base.Strings;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ElementUtils {

    public static MultivaluedMap<String, String> parseFormParameters(Element formElement) {
        MultivaluedMapImpl parameters = new MultivaluedMapImpl();

        Elements inputElements = formElement.getElementsByTag("input");

        for (Element inputElement : inputElements) {

            if (Strings.isNullOrEmpty(inputElement.attr("name"))) {
                continue;
            }

            parameters.add(inputElement.attr("name"), inputElement.attr("value"));
        }

        return parameters;
    }
}
