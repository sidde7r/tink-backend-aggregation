
package se.tink.backend.idcontrol.creditsafe.consumermonitoring.soap;

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
 *         &lt;element name="CopyConsumerResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
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
    "copyConsumerResult"
})
@XmlRootElement(name = "CopyConsumerResponse")
public class CopyConsumerResponse {

    @XmlElement(name = "CopyConsumerResult")
    protected MonitoringResponse copyConsumerResult;

    /**
     * Gets the value of the copyConsumerResult property.
     * 
     * @return
     *     possible object is
     *     {@link MonitoringResponse }
     *     
     */
    public MonitoringResponse getCopyConsumerResult() {
        return copyConsumerResult;
    }

    /**
     * Sets the value of the copyConsumerResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonitoringResponse }
     *     
     */
    public void setCopyConsumerResult(MonitoringResponse value) {
        this.copyConsumerResult = value;
    }

}
