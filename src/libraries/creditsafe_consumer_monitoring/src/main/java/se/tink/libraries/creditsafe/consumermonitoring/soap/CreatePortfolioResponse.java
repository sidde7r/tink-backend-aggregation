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
 *         &lt;element name="CreatePortfolioResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"createPortfolioResult"})
@XmlRootElement(name = "CreatePortfolioResponse")
public class CreatePortfolioResponse {

    @XmlElement(name = "CreatePortfolioResult")
    protected MonitoringResponse createPortfolioResult;

    /**
     * Gets the value of the createPortfolioResult property.
     *
     * @return possible object is {@link MonitoringResponse }
     */
    public MonitoringResponse getCreatePortfolioResult() {
        return createPortfolioResult;
    }

    /**
     * Sets the value of the createPortfolioResult property.
     *
     * @param value allowed object is {@link MonitoringResponse }
     */
    public void setCreatePortfolioResult(MonitoringResponse value) {
        this.createPortfolioResult = value;
    }
}
