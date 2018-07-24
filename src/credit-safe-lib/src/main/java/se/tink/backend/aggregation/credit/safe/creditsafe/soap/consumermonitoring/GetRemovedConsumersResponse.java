
package se.tink.backend.aggregation.credit.safe.creditsafe.soap.consumermonitoring;

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
 *         &lt;element name="GetRemovedConsumersResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
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
    "getRemovedConsumersResult"
})
@XmlRootElement(name = "GetRemovedConsumersResponse")
public class GetRemovedConsumersResponse {

    @XmlElement(name = "GetRemovedConsumersResult")
    protected MonitoringResponse getRemovedConsumersResult;

    /**
     * Gets the value of the getRemovedConsumersResult property.
     * 
     * @return
     *     possible object is
     *     {@link MonitoringResponse }
     *     
     */
    public MonitoringResponse getGetRemovedConsumersResult() {
        return getRemovedConsumersResult;
    }

    /**
     * Sets the value of the getRemovedConsumersResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonitoringResponse }
     *     
     */
    public void setGetRemovedConsumersResult(MonitoringResponse value) {
        this.getRemovedConsumersResult = value;
    }

}
