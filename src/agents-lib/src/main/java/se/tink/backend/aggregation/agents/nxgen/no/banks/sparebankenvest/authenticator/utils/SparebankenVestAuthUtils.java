package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.utils;

import com.google.common.base.Preconditions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.entities.SecurityParamsRequestBody;

public class SparebankenVestAuthUtils {

    public static SecurityParamsRequestBody createSecurityParamsRequestBody(String htmlResponseString) {
        Document doc = Jsoup.parse(htmlResponseString);

    Element waElement =
            doc.getElementsByAttributeValue(
                    SparebankenVestConstants.SecurityParameters.NAME,
                    SparebankenVestConstants.SecurityParameters.WA)
            .first();

        Element wresultElement =
                doc.getElementsByAttributeValue(
                        SparebankenVestConstants.SecurityParameters.NAME,
                        SparebankenVestConstants.SecurityParameters.WRESULT)
                .first();

        Element wctxElement =
                doc.getElementsByAttributeValue(
                        SparebankenVestConstants.SecurityParameters.NAME,
                        SparebankenVestConstants.SecurityParameters.WCTX)
                .first();

        Preconditions.checkState(waElement != null, "Could not parse wa from response.");
        Preconditions.checkState(wresultElement != null, "Could not parse wresult from response.");
        Preconditions.checkState(wctxElement != null, "Could not parse wctx from response.");

        return new SecurityParamsRequestBody(waElement.val(), wresultElement.val(), wctxElement.val());
    }
}
