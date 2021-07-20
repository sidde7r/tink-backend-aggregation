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
 *         &lt;element name="mon_req" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringRequest" minOccurs="0"/>
 *         &lt;element name="objPortfolioInfo" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}PortfolioInfoObject" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"monReq", "objPortfolioInfo"})
@XmlRootElement(name = "UpdatePortfolio")
public class UpdatePortfolio {

    @XmlElement(name = "mon_req")
    protected MonitoringRequest monReq;

    protected PortfolioInfoObject objPortfolioInfo;

    /**
     * Gets the value of the monReq property.
     *
     * @return possible object is {@link MonitoringRequest }
     */
    public MonitoringRequest getMonReq() {
        return monReq;
    }

    /**
     * Sets the value of the monReq property.
     *
     * @param value allowed object is {@link MonitoringRequest }
     */
    public void setMonReq(MonitoringRequest value) {
        this.monReq = value;
    }

    /**
     * Gets the value of the objPortfolioInfo property.
     *
     * @return possible object is {@link PortfolioInfoObject }
     */
    public PortfolioInfoObject getObjPortfolioInfo() {
        return objPortfolioInfo;
    }

    /**
     * Sets the value of the objPortfolioInfo property.
     *
     * @param value allowed object is {@link PortfolioInfoObject }
     */
    public void setObjPortfolioInfo(PortfolioInfoObject value) {
        this.objPortfolioInfo = value;
    }
}
