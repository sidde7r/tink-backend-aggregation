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
 *         &lt;element name="AddPortfolioEmailResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"addPortfolioEmailResult"})
@XmlRootElement(name = "AddPortfolioEmailResponse")
public class AddPortfolioEmailResponse {

    @XmlElement(name = "AddPortfolioEmailResult")
    protected MonitoringResponse addPortfolioEmailResult;

    /**
     * Gets the value of the addPortfolioEmailResult property.
     *
     * @return possible object is {@link MonitoringResponse }
     */
    public MonitoringResponse getAddPortfolioEmailResult() {
        return addPortfolioEmailResult;
    }

    /**
     * Sets the value of the addPortfolioEmailResult property.
     *
     * @param value allowed object is {@link MonitoringResponse }
     */
    public void setAddPortfolioEmailResult(MonitoringResponse value) {
        this.addPortfolioEmailResult = value;
    }
}
