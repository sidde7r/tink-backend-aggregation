package se.tink.backend.aggregation.agents.fraud.creditsafe.soap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/** This class was generated by the JAX-WS RI. JAX-WS RI 2.2.4-b01 Generated source version: 2.2 */
@WebService(name = "GetDataSoap", targetNamespace = "https://webservice.creditsafe.se/getdata/")
@XmlSeeAlso({
    ObjectFactory.class,
    se.tink.backend.aggregation.agents.fraud.creditsafe.soap.xmlschema.ObjectFactory.class
})
public interface GetDataSoap {

    /**
     * @param getDataRequest
     * @return returns https.webservice_creditsafe_se.getdata.GETDATARESPONSE
     */
    @WebMethod(
            operationName = "GetDataBySecure",
            action = "https://webservice.creditsafe.se/getdata/GetDataBySecure")
    @WebResult(
            name = "GetDataBySecureResult",
            targetNamespace = "https://webservice.creditsafe.se/getdata/")
    @RequestWrapper(
            localName = "GetDataBySecure",
            targetNamespace = "https://webservice.creditsafe.se/getdata/",
            className = "https.webservice_creditsafe_se.getdata.GetDataBySecure")
    @ResponseWrapper(
            localName = "GetDataBySecureResponse",
            targetNamespace = "https://webservice.creditsafe.se/getdata/",
            className = "https.webservice_creditsafe_se.getdata.GetDataBySecureResponse")
    public GetDataResponse getDataBySecure(
            @WebParam(
                            name = "GetData_Request",
                            targetNamespace = "https://webservice.creditsafe.se/getdata/")
                    GetDataRequest getDataRequest);
}
