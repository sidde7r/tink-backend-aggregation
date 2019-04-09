package se.tink.libraries.creditsafe.consumermonitoring.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for EmailFilterObject complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EmailFilterObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FilterId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="GroupId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="FilterName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="GroupName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AcceptNumericalValue" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="NumericalValue" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="IsActive" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "EmailFilterObject",
        propOrder = {
            "filterId",
            "groupId",
            "filterName",
            "groupName",
            "acceptNumericalValue",
            "numericalValue",
            "isActive"
        })
public class EmailFilterObject {

    @XmlElement(name = "FilterId")
    protected int filterId;

    @XmlElement(name = "GroupId")
    protected int groupId;

    @XmlElement(name = "FilterName")
    protected String filterName;

    @XmlElement(name = "GroupName")
    protected String groupName;

    @XmlElement(name = "AcceptNumericalValue")
    protected boolean acceptNumericalValue;

    @XmlElement(name = "NumericalValue", required = true, type = Long.class, nillable = true)
    protected Long numericalValue;

    @XmlElement(name = "IsActive")
    protected boolean isActive;

    /** Gets the value of the filterId property. */
    public int getFilterId() {
        return filterId;
    }

    /** Sets the value of the filterId property. */
    public void setFilterId(int value) {
        this.filterId = value;
    }

    /** Gets the value of the groupId property. */
    public int getGroupId() {
        return groupId;
    }

    /** Sets the value of the groupId property. */
    public void setGroupId(int value) {
        this.groupId = value;
    }

    /**
     * Gets the value of the filterName property.
     *
     * @return possible object is {@link String }
     */
    public String getFilterName() {
        return filterName;
    }

    /**
     * Sets the value of the filterName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setFilterName(String value) {
        this.filterName = value;
    }

    /**
     * Gets the value of the groupName property.
     *
     * @return possible object is {@link String }
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the value of the groupName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setGroupName(String value) {
        this.groupName = value;
    }

    /** Gets the value of the acceptNumericalValue property. */
    public boolean isAcceptNumericalValue() {
        return acceptNumericalValue;
    }

    /** Sets the value of the acceptNumericalValue property. */
    public void setAcceptNumericalValue(boolean value) {
        this.acceptNumericalValue = value;
    }

    /**
     * Gets the value of the numericalValue property.
     *
     * @return possible object is {@link Long }
     */
    public Long getNumericalValue() {
        return numericalValue;
    }

    /**
     * Sets the value of the numericalValue property.
     *
     * @param value allowed object is {@link Long }
     */
    public void setNumericalValue(Long value) {
        this.numericalValue = value;
    }

    /** Gets the value of the isActive property. */
    public boolean isIsActive() {
        return isActive;
    }

    /** Sets the value of the isActive property. */
    public void setIsActive(boolean value) {
        this.isActive = value;
    }
}
