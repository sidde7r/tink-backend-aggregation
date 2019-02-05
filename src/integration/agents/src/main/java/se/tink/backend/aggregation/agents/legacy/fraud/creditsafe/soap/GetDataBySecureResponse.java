package se.tink.backend.aggregation.agents.fraud.creditsafe.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetDataBySecureResult" type="{https://webservice.creditsafe.se/getdata/}GETDATA_RESPONSE" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getDataBySecureResult"
})
@XmlRootElement(name = "GetDataBySecureResponse")
public class GetDataBySecureResponse {

    @XmlElement(name = "GetDataBySecureResult")
    protected GetDataResponse getDataBySecureResult;

    /**
     * Gets the value of the getDataBySecureResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetDataResponse }
     *     
     */
    public GetDataResponse getGetDataBySecureResult() {
        return getDataBySecureResult;
    }

    /**
     * Sets the value of the getDataBySecureResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetDataResponse }
     *     
     */
    public void setGetDataBySecureResult(GetDataResponse value) {
        this.getDataBySecureResult = value;
    }

}
