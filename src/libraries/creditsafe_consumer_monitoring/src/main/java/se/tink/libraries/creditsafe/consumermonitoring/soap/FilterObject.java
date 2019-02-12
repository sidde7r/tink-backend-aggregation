
package se.tink.libraries.creditsafe.consumermonitoring.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FilterObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FilterObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Group_EngText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Group_SweText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EngText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SweText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterObject", propOrder = {
    "groupEngText",
    "groupSweText",
    "engText",
    "sweText"
})
public class FilterObject {

    @XmlElement(name = "Group_EngText")
    protected String groupEngText;
    @XmlElement(name = "Group_SweText")
    protected String groupSweText;
    @XmlElement(name = "EngText")
    protected String engText;
    @XmlElement(name = "SweText")
    protected String sweText;

    /**
     * Gets the value of the groupEngText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupEngText() {
        return groupEngText;
    }

    /**
     * Sets the value of the groupEngText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupEngText(String value) {
        this.groupEngText = value;
    }

    /**
     * Gets the value of the groupSweText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupSweText() {
        return groupSweText;
    }

    /**
     * Sets the value of the groupSweText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupSweText(String value) {
        this.groupSweText = value;
    }

    /**
     * Gets the value of the engText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEngText() {
        return engText;
    }

    /**
     * Sets the value of the engText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEngText(String value) {
        this.engText = value;
    }

    /**
     * Gets the value of the sweText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSweText() {
        return sweText;
    }

    /**
     * Sets the value of the sweText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSweText(String value) {
        this.sweText = value;
    }

}
