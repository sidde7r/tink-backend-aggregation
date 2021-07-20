package se.tink.libraries.creditsafe.consumermonitoring.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

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
 *         &lt;element name="RemoveConsumerResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"removeConsumerResult"})
@XmlRootElement(name = "RemoveConsumerResponse")
public class RemoveConsumerResponse {

    @XmlElement(name = "RemoveConsumerResult")
    protected MonitoringResponse removeConsumerResult;

    /**
     * Gets the value of the removeConsumerResult property.
     *
     * @return possible object is {@link MonitoringResponse }
     */
    public MonitoringResponse getRemoveConsumerResult() {
        return removeConsumerResult;
    }

    /**
     * Sets the value of the removeConsumerResult property.
     *
     * @param value allowed object is {@link MonitoringResponse }
     */
    public void setRemoveConsumerResult(MonitoringResponse value) {
        this.removeConsumerResult = value;
    }
}
