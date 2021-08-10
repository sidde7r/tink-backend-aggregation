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
 *         &lt;element name="ChangeStdPortfolioResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"changeStdPortfolioResult"})
@XmlRootElement(name = "ChangeStdPortfolioResponse")
public class ChangeStdPortfolioResponse {

    @XmlElement(name = "ChangeStdPortfolioResult")
    protected MonitoringResponse changeStdPortfolioResult;

    /**
     * Gets the value of the changeStdPortfolioResult property.
     *
     * @return possible object is {@link MonitoringResponse }
     */
    public MonitoringResponse getChangeStdPortfolioResult() {
        return changeStdPortfolioResult;
    }

    /**
     * Sets the value of the changeStdPortfolioResult property.
     *
     * @param value allowed object is {@link MonitoringResponse }
     */
    public void setChangeStdPortfolioResult(MonitoringResponse value) {
        this.changeStdPortfolioResult = value;
    }
}
