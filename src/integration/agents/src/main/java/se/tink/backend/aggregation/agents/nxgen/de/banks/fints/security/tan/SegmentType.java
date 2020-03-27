package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum SegmentType {
    HKIDN("HKIDN"), // Identification
    HKSAL("HKSAL"), // Balance query
    HKTAB("HKTAB"), // Display of the available TAN media
    HKTAU("HKTAU"), // Register or re-register TAN generator
    HKTSY("HKTSY"), // TAN generator synchronization
    HKMTR("HKMTR"), // Register mobile phone connection
    HKMTS("HKMTS"), // Register mobile phone connection
    HKMTF("HKMTF"), // Unlock cellular connection
    HKMTA("HKMTA"), // Change cellular connection
    HKTML("HKTML"), // Deactivate / delete TAN media
    HKKAZ("HKKAZ"), // Account transactions in MT940 format
    HKCAZ("HKCAZ"), // Account transactions in camt format
    HNHBK("HNHBK"),
    HKTAN("HKTAN"),
    HKCSE("HKCSE"),
    HKCSL("HKCSL"),
    HKPAE("HKPAE"),
    HKEKA("HKEKA"),
    DKPAE("DKPAE"),
    HKCDL("HKCDL"),
    HKCML("HKCML"),
    HKDME("HKDME"),
    HKPSA("HKPSA"),
    HKCME("HKCME"),
    DKPSA("DKPSA"),
    HKBME("HKBME"),
    HKDMC("HKDMC"),
    HKPRO("HKPRO"),
    HKCDN("HKCDN"),
    HKDSC("HKDSC"),
    HKCSB("HKCSB"),
    HKCDE("HKCDE"),
    HKKAU("HKKAU"),
    HKCCS("HKCCS"),
    HKCCM("HKCCM"),
    HKCMB("HKCMB"),
    HKSPA("HKSPA"),
    HKCDB("HKCDB"),
    HKAUB("HKAUB"),
    HKBSE("HKBSE"),
    HKBMB("HKBMB"),
    HKBML("HKBML"),
    HKDMB("HKDMB"),
    HKDML("HKDML"),
    HKCDU("HKCDU"),
    HKCSA("HKCSA"),
    HKCSU("HKCSU"),
    HKIPZ("HKIPZ"),
    HKIPS("HKIPS"),
    HKPKB("HKPKB"),
    HKPKA("HKPKA"),
    HKPWE("HKPWE"),
    HKPWL("HKPWL"),
    HKPWB("HKPWB"),
    HKPWA("HKPWA"),
    HKCUB("HKCUB"),
    HKCUM("HKCUM"),
    HKDSB("HKDSB"),
    HKDSE("HKDSE"),
    HKDSW("HKDSW"),
    HKEKP("HKEKP"),
    HKFGB("HKFGB"),
    HKFRD("HKFRD"),
    HKKDM("HKKDM"),
    HKKIF("HKKIF"),
    HKNEA("HKNEA"),
    HKNEZ("HKNEZ"),
    HKWFO("HKWFO"),
    HKWPO("HKWPO"),
    HKFPO("HKFPO"),
    HKWSD("HKWSD"),
    HKPPD("HKPPD"),
    HKQTG("HKQTG"),
    HKTAZ("HKTAZ"),
    HKUTA("HKUTA"),
    HKWDU("HKWDU"),
    HKWFP("HKWFP"),
    HKWOA("HKWOA"),
    HKWPD("HKWPD"),
    HKWPK("HKWPK"),
    HKWPR("HKWPR"),
    HKWPS("HKWPS"),
    HKWSO("HKWSO"),
    DKBKD("DKBKD"),
    DKBKU("DKBKU"),
    DKBUM("DKBUM"),
    DKFDA("DKFDA"),
    DKPSP("DKPSP"),
    DKTLA("DKTLA"),
    DKTLF("DKTLF"),
    DKTSP("DKTSP"),
    DKWAP("DKWAP"),
    DKALE("DKALE"),
    DKALL("DKALL"),
    DKALN("DKALN"),
    DKANA("DKANA"),
    DKANL("DKANL"),
    DKBAZ("DKBAZ"),
    DKBVA("DKBVA"),
    DKBVB("DKBVB"),
    DKBVD("DKBVD"),
    DKBVK("DKBVK"),
    DKBVP("DKBVP"),
    DKBVR("DKBVR"),
    DKBVS("DKBVS"),
    DKDFA("DKDFA"),
    DKDFB("DKDFB"),
    DKDFC("DKDFC"),
    DKDFD("DKDFD"),
    DKDFL("DKDFL"),
    DKDFU("DKDFU"),
    DKDIH("DKDIH"),
    DKDFS("DKDFS"),
    DKDDI("DKDDI"),
    DKDFO("DKDFO"),
    DKDFP("DKDFP"),
    DKDPF("DKDPF"),
    DKDFE("DKDFE"),
    DKDEF("DKDEF"),
    DKDOF("DKDOF"),
    DKFAF("DKFAF"),
    DKGBA("DKGBA"),
    DKGBS("DKGBS"),
    DKKAU("DKKAU"),
    DKKKA("DKKKA"),
    DKKKS("DKKKS"),
    DKKKU("DKKKU"),
    DKKSB("DKKSB"),
    DKKSP("DKKSP"),
    DKQUO("DKQUO"),
    DKQUT("DKQUT"),
    DKVVD("DKVVD"),
    DKVVU("DKVVU"),
    DKWDG("DKWDG"),
    DKWGV("DKWGV"),
    DKWLV("DKWLV"),
    DKNZP("DKNZP"),
    DKFOP("DKFOP"),
    DKFPO("DKFPO"),
    DKWOP("DKWOP"),
    DKWVB("DKWVB"),
    DKZDF("DKZDF"),
    DKZDL("DKZDL"),
    DKWOK("DKWOK"),
    DKWDH("DKWDH"),
    DKBVE("DKBVE"),
    DKPTZ("DKPTZ"),
    DKEEA("DKEEA"),
    UNKNOWN("UNKNOWN");

    SegmentType(String segmentType) {
        this.segmentType = segmentType;
    }

    public static SegmentType of(String segmentType) {
        return Arrays.stream(values())
                .filter(segment -> segment.segmentType.equals(segmentType))
                .findFirst()
                .orElseGet(
                        () -> {
                            log.warn("Could not map {}", segmentType);
                            return UNKNOWN;
                        });
    }

    private String segmentType;

    public String getSegmentType() {
        return segmentType;
    }
}
