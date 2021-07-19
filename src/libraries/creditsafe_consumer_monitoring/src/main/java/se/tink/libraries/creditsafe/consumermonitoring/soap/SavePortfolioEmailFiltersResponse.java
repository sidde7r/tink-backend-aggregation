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
 *         &lt;element name="SavePortfolioEmailFiltersResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"savePortfolioEmailFiltersResult"})
@XmlRootElement(name = "SavePortfolioEmailFiltersResponse")
public class SavePortfolioEmailFiltersResponse {

    @XmlElement(name = "SavePortfolioEmailFiltersResult")
    protected MonitoringResponse savePortfolioEmailFiltersResult;

    /**
     * Gets the value of the savePortfolioEmailFiltersResult property.
     *
     * @return possible object is {@link MonitoringResponse }
     */
    public MonitoringResponse getSavePortfolioEmailFiltersResult() {
        return savePortfolioEmailFiltersResult;
    }

    /**
     * Sets the value of the savePortfolioEmailFiltersResult property.
     *
     * @param value allowed object is {@link MonitoringResponse }
     */
    public void setSavePortfolioEmailFiltersResult(MonitoringResponse value) {
        this.savePortfolioEmailFiltersResult = value;
    }
}
