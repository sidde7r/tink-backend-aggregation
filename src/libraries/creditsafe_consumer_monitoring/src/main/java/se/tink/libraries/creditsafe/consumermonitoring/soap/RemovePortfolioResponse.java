package se.tink.libraries.creditsafe.consumermonitoring.soap;

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
 *         &lt;element name="RemovePortfolioResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"removePortfolioResult"})
@XmlRootElement(name = "RemovePortfolioResponse")
public class RemovePortfolioResponse {

    @XmlElement(name = "RemovePortfolioResult")
    protected MonitoringResponse removePortfolioResult;

    /**
     * Gets the value of the removePortfolioResult property.
     *
     * @return possible object is {@link MonitoringResponse }
     */
    public MonitoringResponse getRemovePortfolioResult() {
        return removePortfolioResult;
    }

    /**
     * Sets the value of the removePortfolioResult property.
     *
     * @param value allowed object is {@link MonitoringResponse }
     */
    public void setRemovePortfolioResult(MonitoringResponse value) {
        this.removePortfolioResult = value;
    }
}
