package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/** This class was generated by the JAX-WS RI. JAX-WS RI 2.2.4-b01 Generated source version: 2.2 */
@WebServiceClient(
        name = "MobileWS_v2",
        targetNamespace = "http://edenred.se/",
        wsdlLocation = "https://www.edenred.se/MobileWS/MobileWS_v2.asmx?WSDL")
public class MobileWSV2 extends Service {

    private static final URL MOBILEWSV2_WSDL_LOCATION;
    private static final WebServiceException MOBILEWSV2_EXCEPTION;
    private static final QName MOBILEWSV2_QNAME = new QName("http://edenred.se/", "MobileWS_v2");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("https://www.edenred.se/MobileWS/MobileWS_v2.asmx?WSDL");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        MOBILEWSV2_WSDL_LOCATION = url;
        MOBILEWSV2_EXCEPTION = e;
    }

    public MobileWSV2() {
        super(__getWsdlLocation(), MOBILEWSV2_QNAME);
    }

    public MobileWSV2(WebServiceFeature... features) {
        super(__getWsdlLocation(), MOBILEWSV2_QNAME, features);
    }

    public MobileWSV2(URL wsdlLocation) {
        super(wsdlLocation, MOBILEWSV2_QNAME);
    }

    public MobileWSV2(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, MOBILEWSV2_QNAME, features);
    }

    public MobileWSV2(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public MobileWSV2(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /** @return returns MobileWSV2Soap */
    @WebEndpoint(name = "MobileWS_v2Soap")
    public MobileWSV2Soap getMobileWSV2Soap() {
        return super.getPort(
                new QName("http://edenred.se/", "MobileWS_v2Soap"), MobileWSV2Soap.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.
     *     Supported features not in the <code>features</code> parameter will have their default
     *     values.
     * @return returns MobileWSV2Soap
     */
    @WebEndpoint(name = "MobileWS_v2Soap")
    public MobileWSV2Soap getMobileWSV2Soap(WebServiceFeature... features) {
        return super.getPort(
                new QName("http://edenred.se/", "MobileWS_v2Soap"), MobileWSV2Soap.class, features);
    }

    private static URL __getWsdlLocation() {
        if (MOBILEWSV2_EXCEPTION != null) {
            throw MOBILEWSV2_EXCEPTION;
        }
        return MOBILEWSV2_WSDL_LOCATION;
    }
}
