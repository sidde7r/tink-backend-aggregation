package se.tink.backend.aggregation.agents.fraud.creditsafe.soap;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the https.webservice_creditsafe_se.getdata package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: https.webservice_creditsafe_se.getdata
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetDataResponse }
     * 
     */
    public GetDataResponse createGETDATARESPONSE() {
        return new GetDataResponse();
    }

    /**
     * Create an instance of {@link GetDataBySecureResponse }
     * 
     */
    public GetDataBySecureResponse createGetDataBySecureResponse() {
        return new GetDataBySecureResponse();
    }

    /**
     * Create an instance of {@link GetDataBySecure }
     * 
     */
    public GetDataBySecure createGetDataBySecure() {
        return new GetDataBySecure();
    }

    /**
     * Create an instance of {@link GetDataRequest }
     * 
     */
    public GetDataRequest createGETDATAREQUEST() {
        return new GetDataRequest();
    }

    /**
     * Create an instance of {@link Error }
     * 
     */
    public Error createERROR() {
        return new Error();
    }

    /**
     * Create an instance of {@link Account }
     * 
     */
    public Account createAccount() {
        return new Account();
    }

    /**
     * Create an instance of {@link GetDataResponse.Parameters }
     * 
     */
    public GetDataResponse.Parameters createGETDATARESPONSEParameters() {
        return new GetDataResponse.Parameters();
    }

}
