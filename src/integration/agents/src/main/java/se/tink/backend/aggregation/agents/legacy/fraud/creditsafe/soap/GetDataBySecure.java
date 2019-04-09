package se.tink.backend.aggregation.agents.fraud.creditsafe.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetData_Request" type="{https://webservice.creditsafe.se/getdata/}GETDATA_REQUEST" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"getDataRequest"})
@XmlRootElement(name = "GetDataBySecure")
public class GetDataBySecure {

    @XmlElement(name = "GetData_Request")
    protected GetDataRequest getDataRequest;

    /**
     * Gets the value of the getDataRequest property.
     *
     * @return possible object is {@link GetDataRequest }
     */
    public GetDataRequest getGetDataRequest() {
        return getDataRequest;
    }

    /**
     * Sets the value of the getDataRequest property.
     *
     * @param value allowed object is {@link GetDataRequest }
     */
    public void setGetDataRequest(GetDataRequest value) {
        this.getDataRequest = value;
    }
}
