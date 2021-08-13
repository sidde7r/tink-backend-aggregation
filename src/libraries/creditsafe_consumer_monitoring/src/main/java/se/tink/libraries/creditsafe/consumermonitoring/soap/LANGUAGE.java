package se.tink.libraries.creditsafe.consumermonitoring.soap;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Java class for LANGUAGE.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <p>
 *
 * <pre>
 * &lt;simpleType name="LANGUAGE">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SWE"/>
 *     &lt;enumeration value="EN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "LANGUAGE")
@XmlEnum
public enum LANGUAGE {
    SWE,
    EN;

    public String value() {
        return name();
    }

    public static LANGUAGE fromValue(String v) {
        return valueOf(v);
    }
}
