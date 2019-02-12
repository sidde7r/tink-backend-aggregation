
package se.tink.libraries.creditsafe.consumermonitoring.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResultCountersRespObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResultCountersRespObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="StartPosition" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="EndPosition" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="PageSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="TotalCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultCountersRespObject", propOrder = {
    "startPosition",
    "endPosition",
    "pageSize",
    "totalCount"
})
public class ResultCountersRespObject {

    @XmlElement(name = "StartPosition")
    protected int startPosition;
    @XmlElement(name = "EndPosition")
    protected int endPosition;
    @XmlElement(name = "PageSize")
    protected int pageSize;
    @XmlElement(name = "TotalCount")
    protected int totalCount;

    /**
     * Gets the value of the startPosition property.
     * 
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * Sets the value of the startPosition property.
     * 
     */
    public void setStartPosition(int value) {
        this.startPosition = value;
    }

    /**
     * Gets the value of the endPosition property.
     * 
     */
    public int getEndPosition() {
        return endPosition;
    }

    /**
     * Sets the value of the endPosition property.
     * 
     */
    public void setEndPosition(int value) {
        this.endPosition = value;
    }

    /**
     * Gets the value of the pageSize property.
     * 
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the value of the pageSize property.
     * 
     */
    public void setPageSize(int value) {
        this.pageSize = value;
    }

    /**
     * Gets the value of the totalCount property.
     * 
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Sets the value of the totalCount property.
     * 
     */
    public void setTotalCount(int value) {
        this.totalCount = value;
    }

}
