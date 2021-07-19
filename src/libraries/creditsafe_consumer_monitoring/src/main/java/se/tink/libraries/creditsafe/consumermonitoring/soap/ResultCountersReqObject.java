package se.tink.libraries.creditsafe.consumermonitoring.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Java class for ResultCountersReqObject complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ResultCountersReqObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="StartPosition" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="PageSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "ResultCountersReqObject",
        propOrder = {"startPosition", "pageSize"})
public class ResultCountersReqObject {

    @XmlElement(name = "StartPosition")
    protected int startPosition;

    @XmlElement(name = "PageSize")
    protected int pageSize;

    /** Gets the value of the startPosition property. */
    public int getStartPosition() {
        return startPosition;
    }

    /** Sets the value of the startPosition property. */
    public void setStartPosition(int value) {
        this.startPosition = value;
    }

    /** Gets the value of the pageSize property. */
    public int getPageSize() {
        return pageSize;
    }

    /** Sets the value of the pageSize property. */
    public void setPageSize(int value) {
        this.pageSize = value;
    }
}
