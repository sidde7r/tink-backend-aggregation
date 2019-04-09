package se.tink.libraries.creditsafe.consumermonitoring.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for MonitoringResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="MonitoringResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ObjectList" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}ArrayOfAnyType" minOccurs="0"/>
 *         &lt;element name="Status" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}STATUS"/>
 *         &lt;element name="Error" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}Error" minOccurs="0"/>
 *         &lt;element name="ResultCounters" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}ResultCountersRespObject" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "MonitoringResponse",
        propOrder = {"objectList", "status", "error", "resultCounters"})
public class MonitoringResponse {

    @XmlElement(name = "ObjectList")
    protected ArrayOfAnyType objectList;

    @XmlElement(name = "Status", required = true)
    protected STATUS status;

    @XmlElement(name = "Error")
    protected Error error;

    @XmlElement(name = "ResultCounters")
    protected ResultCountersRespObject resultCounters;

    /**
     * Gets the value of the objectList property.
     *
     * @return possible object is {@link ArrayOfAnyType }
     */
    public ArrayOfAnyType getObjectList() {
        return objectList;
    }

    /**
     * Sets the value of the objectList property.
     *
     * @param value allowed object is {@link ArrayOfAnyType }
     */
    public void setObjectList(ArrayOfAnyType value) {
        this.objectList = value;
    }

    /**
     * Gets the value of the status property.
     *
     * @return possible object is {@link STATUS }
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value allowed object is {@link STATUS }
     */
    public void setStatus(STATUS value) {
        this.status = value;
    }

    /**
     * Gets the value of the error property.
     *
     * @return possible object is {@link Error }
     */
    public Error getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     *
     * @param value allowed object is {@link Error }
     */
    public void setError(Error value) {
        this.error = value;
    }

    /**
     * Gets the value of the resultCounters property.
     *
     * @return possible object is {@link ResultCountersRespObject }
     */
    public ResultCountersRespObject getResultCounters() {
        return resultCounters;
    }

    /**
     * Sets the value of the resultCounters property.
     *
     * @param value allowed object is {@link ResultCountersRespObject }
     */
    public void setResultCounters(ResultCountersRespObject value) {
        this.resultCounters = value;
    }
}
