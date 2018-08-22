package se.tink.backend.aggregation.credit.safe.agent.creditsafe.soap;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "GetData", targetNamespace = "https://webservice.creditsafe.se/getdata/", wsdlLocation = "https://webservice.creditsafe.se/getdata/getdata.asmx")
public class GetData
    extends Service
{

    private final static URL GETDATA_WSDL_LOCATION;
    private final static WebServiceException GETDATA_EXCEPTION;
    private final static QName GETDATA_QNAME = new QName("https://webservice.creditsafe.se/getdata/", "GetData");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("https://webservice.creditsafe.se/getdata/getdata.asmx");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        GETDATA_WSDL_LOCATION = url;
        GETDATA_EXCEPTION = e;
    }

    public GetData() {
        super(__getWsdlLocation(), GETDATA_QNAME);
    }

    public GetData(WebServiceFeature... features) {
        super(__getWsdlLocation(), GETDATA_QNAME, features);
    }

    public GetData(URL wsdlLocation) {
        super(wsdlLocation, GETDATA_QNAME);
    }

    public GetData(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, GETDATA_QNAME, features);
    }

    public GetData(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public GetData(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns GetDataSoap
     */
    @WebEndpoint(name = "GetDataSoap")
    public GetDataSoap getGetDataSoap() {
        return super.getPort(new QName("https://webservice.creditsafe.se/getdata/", "GetDataSoap"), GetDataSoap.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns GetDataSoap
     */
    @WebEndpoint(name = "GetDataSoap")
    public GetDataSoap getGetDataSoap(WebServiceFeature... features) {
        return super.getPort(new QName("https://webservice.creditsafe.se/getdata/", "GetDataSoap"), GetDataSoap.class, features);
    }

    private static URL __getWsdlLocation() {
        if (GETDATA_EXCEPTION!= null) {
            throw GETDATA_EXCEPTION;
        }
        return GETDATA_WSDL_LOCATION;
    }

}
