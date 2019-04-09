package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.collect.ImmutableList;
import java.security.PublicKey;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.JsonWebKeySet;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class JwkTest {
    private static final ImmutableList<String> JWKS =
            ImmutableList.<String>builder()
                    .add(
                            "{\"keys\":[{\"kty\":\"EC\",\"kid\":\"SAbr2-DS2NdgccShpSU7kC\",\"use\":\"sig\",\"x\":\"n1SQPjumx_ZAU6ukxXZS7kETGLBcLT4RLXfedVtf9rk\",\"y\":\"cpiWNUlww78CXxMKnWOXP2rNvx1BlZkWYAzrzSrCRtw\",\"crv\":\"P-256\"},{\"kty\":\"EC\",\"kid\":\"SXjcp5OWYxaWroVgCx16Wr\",\"use\":\"sig\",\"x\":\"wst2qJtJXhQy2WhoMvM2nE_s1HBd4igF0NBX2pyPJFjqD4uWwQLJYMQ10yrzmETC\",\"y\":\"aTWDPu5A9udydET1FxUPNcX5Y0HOQCQq-KQ0lrGVt9Yg5jSqwv1zk1du1fQHLygU\",\"crv\":\"P-384\"},{\"kty\":\"EC\",\"kid\":\"fZeMGGaQeEYwnyuDAkey1Q\",\"use\":\"sig\",\"x\":\"AWHl366kRT5nw1Z7rmIyFIi_HbHY6JxbZA63AoIntVBPdEML_T7rchkrJZgPauIHlmxS1ZRW2lzi5L4ObhJ0uJMb\",\"y\":\"ARpyKLsdT4J-zHR5nAsfl4rIyIGl7qg_X0nzqMYYU3t4f-4IMHXYGiuTc5tCBMspUKN-nh96PMlVuvXfIuCn1DmZ\",\"crv\":\"P-521\"},{\"kty\":\"RSA\",\"kid\":\"wbqSpKrKM85RUY7KQueqwG\",\"use\":\"sig\",\"n\":\"xJKnPUHf-X3qN2o1tJ_0tP8PyiHRCdvMmQJDsg8WTHuXo6EETUo-OX7xnEg7HwR_6-646V0gk-eSqdxRsa2dOWkehsRRTqFqwrQUj2w-OU4KwWPktl58Nwj1bmyMAzqSjY4S0irEDl8RQ35lT1r-THm1gYkFZZefW7UsTDqMNDMrbi7kcCZQBVn80YBA1_h8F4cpl96w63kxhcZyS4KxaNiZQrufL33etY6yw2I2QKkMJowUjbJ5Ip2vxZ67D-xQR2si0KAVYel6x8Qe_udRDfuoHEKvLx0rGPwr7tE4zVMRbAugY67SNeoqPkau-ERBeB5E-8Ntu2l4K2so7n8giQ\",\"e\":\"AQAB\"},{\"kty\":\"RSA\",\"kid\":\"3oGKedk4ng19gWrWa91UC6\",\"use\":\"sig\",\"n\":\"q5HSVjjE_h880pnsn3IWbgw8cggUet3MR0X0nXs5LpMqwsu4rO5qhcN2culh9lrGPJmcpamn9yggp4XmJAIjcx4DPAReDkcFHbjIGvDWE5RlsrAnAL6Y43GYm_12pwflAkB0j5lFzyQiqk82NdlUc-ouOAMkG3Gyd7Vui5hdF-1UYBxMKIusnOCZRzT7StT56qrLdwuX1bmRY8YahdC1zpsb9-sbM27hpG00HR82Oq3mL2y5zZWfyNlyxw4LHOc8RtJZ4AH-CmJoLV1wj82fsXQeV1pVUK3BxlaMd-gxHp4f2jVLw__gNKNR8nPy-nDClZLSezfW5hXJ8IDr3r1QIw\",\"e\":\"AQAB\"},{\"kty\":\"EC\",\"kid\":\"Hlb2ykWMYCnH9kx6ogcynW\",\"use\":\"sig\",\"x\":\"nOYlJGCwQ-T9OvbOWNZ_sQIdbBXMEIZ8-3icacj8-HU\",\"y\":\"oe1iT5zUzo29Ou5U7VnoD5Bp3ch5pmxY0t6_lrqn-i4\",\"crv\":\"P-256\"},{\"kty\":\"EC\",\"kid\":\"LY3SjZWKBV2Lns3laYXV6a\",\"use\":\"sig\",\"x\":\"AacfqkoGE6n5BRVtOi53cATo0MzrmB-ib25PBcaFLHgFcNfF0Ea7x05jWMRix6gUHi7q_snir6nQmO4phQeoqQ1D\",\"y\":\"AQ1zsY2AcDCAwLfxmVLbf9cIlTuMqGZSakBunjHXiwoaMTX1Bcj8QJ8AUSm7tXUu4zr3jeZGbtGe9XIWAp28bf-P\",\"crv\":\"P-521\"},{\"kty\":\"EC\",\"kid\":\"S_UOAO8614Yakn0ckizcSW\",\"use\":\"sig\",\"x\":\"RqRTksj645tFSd5dzF3tovnQCHPWnuHKoJKhDrV0yM6NaLvPQCwoKBJ5UQwzD3zT\",\"y\":\"OmXI05mXpcJ-y72uv6t7saMKeBqznwKm5sTD2zqH_mURkhwY0XqynUf5q-zBLb1u\",\"crv\":\"P-384\"},{\"kty\":\"RSA\",\"kid\":\"-LbATVPTYIlMYNynNR4im3\",\"use\":\"sig\",\"n\":\"w-euytnlH5FuHFtyWZY7uBLmYQsQgH4m0wOhU8yQ6aMI4XyyjynDx8LGSdext-hqJ3Yz65CBT_6W9ybTg5aJfOVvqyM_8G4cBfB2b6kibUJVmKiq1CTJQxrZY1O2vw_dt-VenP8FIp4G9rbyGhD0k3G0KSPRHvIRzoWjoRIwRvUJG8BGm_jMTsFNvJMW8LEvKBkTr2Y6fSvi2p25eZedZAz5HzEnGIzE3FXggyefhBseYpywe6-qhc37eVambfHNVYg5ERkTLW2wl-JO-mZ2mPLVN0NZSFpvAS6R099NHd4xuTjRMfzXa1J5uMaGXrr93Blo4VGDwjY_b95YiKo_3w\",\"e\":\"AQAB\"},{\"kty\":\"EC\",\"kid\":\"41HIhZCXh3pkK0a0LvADKO\",\"use\":\"sig\",\"x\":\"MHoTfJZmvDHokti2kLs5Xhwso7Mn13KmZE3i_iFcJzg\",\"y\":\"abfhpxQj3s6PZolxtXgaIiu4Gonix1gYzV_XWqwxiT8\",\"crv\":\"P-256\"},{\"kty\":\"EC\",\"kid\":\"LYXab_9dG8Cur1h15EEVo0\",\"use\":\"sig\",\"x\":\"n-XuSgXDsRB3pV1q-7UQNfx6DqGlfxt9WHkMU3DQRR5MtgCfjPazDV4LTHxt1PN4\",\"y\":\"indT-L9d8yPPQQXMgETrj5tn1mGM4VUUfjH_962O6mOiOwL43TEFNNJjMf37f4Ds\",\"crv\":\"P-384\"},{\"kty\":\"EC\",\"kid\":\"jSFZEG1mSdOZiQopZ2LW_4\",\"use\":\"sig\",\"x\":\"AYMCSXWIijZH8wxy2_JM7wxI342lotWJDR7mgZrl8MdbQR4Eh9T5McP7-JGS5veHuzlu7Jzh5M8h7K0aYUrQq4EU\",\"y\":\"AOlwuy4OGX2laL5IUZxf-Z1_NDC2sCNE_W5zdJADjrA_WRdNg9UD1kODt2dTUFSwLhR15CYmPYqEsDpT1yIcE4RX\",\"crv\":\"P-521\"}]}")
                    .add(
                            "{\"keys\":[{\"e\":\"AQAB\",\"kid\":\"FwIg5Wnp5i8CWNZj6gAFsvIMixw\",\"kty\":\"RSA\",\"n\":\"vQHOGwW87fnxy-oOpxC8NZxa_tNIgUSftaIS9X966q-G6P2cQRt-x7AZVXLutuepQHb0B9KCpxyCLesQ0anFnO34Q_ohOyZaLxuSbRb3NbhZMxQVOSZ9HevxK_gNeNP7DxI8cekTbP8qRP_ldzZZfvwM2ij_ajONwcppX8JEkXnSHF-TZN3d4IZXz7G7thMCMv5Fds26O6KbSbYbX1QF8iLKZFCnHaS9Lq_6yvL6p07LhbciJmDXPLKeUuywuGiZbQiLKGqhU22GuQ59DfvAQwMYNn7rOZdzUUPSsXqg9-ZSnM-aYXOXpGsp-1-fNsXmmdfAa-C1q11xXwSau2lsow\",\"x5t\":\"62tsn9qEraSZMOyyBlRrdaX0hQ8=\",\"x5t#256\":\"H6kIt6THRLFfx7KWge8Q1n9-Kphy8JrZXZkOZq8CKkI=\",\"x5u\":\"https://keystore.openbanking.org.uk/0015800000jf9GgAAI/FwIg5Wnp5i8CWNZj6gAFsvIMixw.pem\",\"x5c\":[\"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\"],\"use\":\"enc\"},{\"e\":\"AQAB\",\"kid\":\"h9TIb7rOW1goIe96LmEcKT7RBgU\",\"kty\":\"RSA\",\"n\":\"2DnV5H2mm4SKMySWDyJV7B_0JJ2alowhriflZS_N3pDcaWI1x_BQkd-DZ9WDZLm_QvgCGJISyLvGyaneINwDZajQYj9ESDoQGf0ZKyFnNjyigKu1xKLb4VZuJJQp1wMK7rnZVtA1FcOBuU2G1z3GsftvTO92BZlJYs8xHwrXTCOEfIigjEoDqA0iP9DTn-UF6S8AXglDXko8A1H2AfSA4U3Hm-TUbheVyp5BVSM3MICGKXiZ51mNIiNTNp_Qi1IctZLLZ5cdilr4TJFZgd2mHv1cbUvMooFCQGDoMOOKUtAJqUsMkoP3N49aKyZb0Vys4J3jjF0h-YZe_DzoXMfpSw\",\"x5t\":\"8PXbv7xoDngVIZyeQB5ivwIiE4U=\",\"x5t#256\":\"zg2ras5BSMTAFjPnu9OEGS78hVNAM6KHh4WzVShWKnU=\",\"x5u\":\"https://keystore.openbanking.org.uk/0015800000jf9GgAAI/h9TIb7rOW1goIe96LmEcKT7RBgU.pem\",\"x5c\":[\"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\"],\"use\":\"sig\"},{\"e\":\"AQAB\",\"kid\":\"y5Yyo8Y6WhPXa_4yU7x12ZB7q_k\",\"kty\":\"RSA\",\"n\":\"nG_riUUebep71V2iSIRSZMFSIxYjwKRaPcbzsZY39Qdb5ZtROm59rf9slbQYDU5GK3_mTfEJaUEyodD8PAnKpj7Hs0UTgtBKCAd2i2_GRJHjVDjWgs7OSOEQBG0ZdVdsET0x_WFL9GqhrAAk1zWdCsKBwmpfh2kU-ea_RFUcEqumlEL6KBQCKQKIPLuumfsgI68mvhrEg5K-_AuYgRTkwWHWqORtnoyxeko-DOq8fnVfuuuikROKaPgJgOzUEAlFJbxIYgUibuoeQEY44vmXfIr8aN00Pj10_P5PKcjkr3pTMbSN9P2W9lK6i19S2KOwW8zqDJZeTkZ0pKrG6n0u0w\",\"x5u\":\"https://keystore.openbanking.org.uk/0015800000jf9GgAAI/y5Yyo8Y6WhPXa_4yU7x12ZB7q_k.pem\",\"x5c\":[\"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\"],\"x5t\":\"-MDVgi70HEIHXx1jy-oBhEGQQfc=\",\"x5t#256\":\"7ezCwlnoYFlxnMItHj518sTlXC5b03EP_q8rSwkYeOQ=\",\"use\":\"sig\"},{\"e\":\"AQAB\",\"kid\":\"3gA6w9uI3Frhi76GrIrfml06RdA\",\"kty\":\"RSA\",\"n\":\"yPg1BkbxqvLeAuDAMNoXe6808TdeSGtMaK_PPNe9OHWbmux7gqoP1R0Ml3ehzBE5IknEJfsqQ49U02WCt-shE1TKXOzy3iQQVsbCAP2-2diysHvM7446PTvMLBiBlUV4ZEf1MVcYANO902sGi_QW6c9lAgH2t4SPvZZLtmTgVSkWiLmqyhb62A7tqHXwT_soZLJ91VxQZA8BL1OuLF0pvADF2KQ4ALRUDF0SlQsPJuOHdFkpSqahwb317QKObcVAZNopLTHgh6uzhu-z6lNTpQ1ND_GFS8djoDTuP1re8hxrNkP_i8Fsi0pZ1cL6ODbJxiW8rfZJmL-q5_G6TZFHOw\",\"x5u\":\"https://keystore.openbanking.org.uk/0015800000jf9GgAAI/3gA6w9uI3Frhi76GrIrfml06RdA.pem\",\"x5c\":[\"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\"],\"x5t\":\"e3eft5AbZe960AN8iK3RFmz6SwQ=\",\"x5t#256\":\"ypXrQhOjE7VAX66lgwcVC2vGwJTLkx1OaPPiaYQ4hk4=\",\"use\":\"sig\"}]}")
                    .add(
                            "{\"keys\":[{\"e\":\"AQAB\",\"kid\":\"xC-7CSys27WtbpY1STFgXAOSgiM\",\"kty\":\"RSA\",\"n\":\"kolpoLwPvA6aN0BidpS3QOdZ5LCH_TDHcb3JabFvwmjIwW30QywPpsOtoXBdVaNt453tzAUGnbNdH1xJ3By_-3qW3WoEYogf2ceagLTUKkrI2SS8T2GOIhkIqRXwc-K6B-1rzyHPyynPVmplnVzMk21EewNbWhqSdsOurYxnmiPYCnsmKERgm3igMbpuME_CC_5KhRu3Vx1-GlkQxm5OoG11wyr_9lX2vcEODwpApy4FNhoXvqJnVQfjKpFrE3RiqO8wbINqctoGffVgrbJ_cRyphwLMAPXS9uChlI5DlljgcSgQM9gh0PIa_CIyYxO2MZFAPefWffn68X5JUDFP2w\",\"x5t\":\"1koNlqDvFhFFIMW_THRzEg_fC10=\",\"x5t#256\":\"yrr8MF0tPk7r9NlSyyEXmwAIW5xVMxrtQLQBxxoDs3A=\",\"x5u\":\"https://keystore.openbanking.org.uk/0015800000jf8aKAAQ/xC-7CSys27WtbpY1STFgXAOSgiM.pem\",\"x5c\":[\"MIIFxjCCBK6gAwIBAgIEWf8O1jANBgkqhkiG9w0BAQsFADBEMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxHzAdBgNVBAMTFk9wZW5CYW5raW5nIElzc3VpbmcgQ0EwHhcNMTcxMjE5MTEwODAzWhcNMTkwMTE5MTEzODAzWjBgMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZBgNVBAsTEjAwMTU4MDAwMDBqZjhhS0FBUTEeMBwGA1UEAxMVYmdock9xWlVNZ0JUVjA3ZUZjeWRmMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkolpoLwPvA6aN0BidpS3QOdZ5LCH/TDHcb3JabFvwmjIwW30QywPpsOtoXBdVaNt453tzAUGnbNdH1xJ3By/+3qW3WoEYogf2ceagLTUKkrI2SS8T2GOIhkIqRXwc+K6B+1rzyHPyynPVmplnVzMk21EewNbWhqSdsOurYxnmiPYCnsmKERgm3igMbpuME/CC/5KhRu3Vx1+GlkQxm5OoG11wyr/9lX2vcEODwpApy4FNhoXvqJnVQfjKpFrE3RiqO8wbINqctoGffVgrbJ/cRyphwLMAPXS9uChlI5DlljgcSgQM9gh0PIa/CIyYxO2MZFAPefWffn68X5JUDFP2wIDAQABo4ICojCCAp4wHwYDVR0RBBgwFoIUYXBpLm5hdGlvbndpZGUuY28udWswDgYDVR0PAQH/BAQDAgeAMCAGA1UdJQEB/wQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjCCAVIGA1UdIASCAUkwggFFMIIBQQYLKwYBBAGodYEGAQEwggEwMDUGCCsGAQUFBwIBFilodHRwOi8vb2IudHJ1c3Rpcy5jb20vcHJvZHVjdGlvbi9wb2xpY2llczCB9gYIKwYBBQUHAgIwgekMgeZUaGlzIENlcnRpZmljYXRlIGlzIHNvbGVseSBmb3IgdXNlIHdpdGggT3BlbiBCYW5raW5nIExpbWl0ZWQgYW5kIGFzc29jaWF0ZWQgT3BlbiBCYW5raW5nIFNlcnZpY2VzLiBJdHMgcmVjZWlwdCwgcG9zc2Vzc2lvbiBvciB1c2UgY29uc3RpdHV0ZXMgYWNjZXB0YW5jZSBvZiB0aGUgT3BlbiBCYW5raW5nIExpbWl0ZWQgQ2VydGlmaWNhdGUgUG9saWN5IGFuZCByZWxhdGVkIGRvY3VtZW50cyB0aGVyZWluLjByBggrBgEFBQcBAQRmMGQwJgYIKwYBBQUHMAGGGmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vY3NwMDoGCCsGAQUFBzAChi5odHRwOi8vb2IudHJ1c3Rpcy5jb20vcHJvZHVjdGlvbi9pc3N1aW5nY2EuY3J0MD8GA1UdHwQ4MDYwNKAyoDCGLmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9wcm9kdWN0aW9uL2lzc3VpbmdjYS5jcmwwHwYDVR0jBBgwFoAUn0m/TjanrMMPK8hH9B8pG8B5CwUwHQYDVR0OBBYEFEyZYEXOOz3jNrmMCChuEguWm4TPMA0GCSqGSIb3DQEBCwUAA4IBAQBGWCrqGstSpH98bjP5mrjffk6Vop15ea3ChUYtcnXxiIISu3FBM77UZyS0i6W7LZ0EFhCi4zZGL43xBKiU0KAeMxQB/GkUuGEtPQy0wuqKE+BpcwYSqU0GT5VPrXVQcezmsRE+vbi58Ph1pl+kt/jQil3c+oop6/D+nqunlGxnIOl03BW6cSJ1OfPvu13zKCvBrj/Iip/XOju5ZLoKoSN0BMefKH8HSMz9CdxHEUmnwzaD7330QvSwdwr57U1+uEj9ZJduPPU2g3c3g77YoOySxNg8xVQEJ0ntwIk6lxpxDRqL+YmdSxL1jg/zxtbrIlYs0kiDI+PMawsIsa3fqTtG\"],\"use\":\"enc\"},{\"e\":\"AQAB\",\"kid\":\"fkA_HIQo4-w12ocNyujVZLLiLLc\",\"kty\":\"RSA\",\"n\":\"vhPgxUT2B-tVxhfnY2AzGyiEmkiQ-XOj-rGPxGMuyhMwSRo63PWIzBSQBoEMtFvml4Ww7NfTscXdsoB5r0O7oBwSE6Ugo0VpiRbpiAiCLkxDSKN_XZZbEqRG5kRs76oMOzdjRfUJCgulKpI11xIJNSCkUDE9HUBhiXu9rGt-4FR6VHagc9dL0KWUzgKx26LNS6ubDxIxh_v-TzxbbrPqKDU9c8JbpAQNJzm1MB7GKxbZXJIAa0k9yurYynWgEgnjJ0oRtKhQfCcLKQFMV5p9YbCu1NTWvbikNzDbKeBqSlhbM8o68HP3pWajm74nBbhBqP9D1DdcpQ9BKBZ8epm98Q\",\"x5u\":\"https://keystore.openbanking.org.uk/0015800000jf8aKAAQ/fkA_HIQo4-w12ocNyujVZLLiLLc.pem\",\"x5c\":[\"MIIFuzCCBKOgAwIBAgIEWf8ZNjANBgkqhkiG9w0BAQsFADBEMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxHzAdBgNVBAMTFk9wZW5CYW5raW5nIElzc3VpbmcgQ0EwHhcNMTgwMjE5MTYwMDA3WhcNMTkwMzE5MTYzMDA3WjBgMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZBgNVBAsTEjAwMTU4MDAwMDBqZjhhS0FBUTEeMBwGA1UEAxMVYmdock9xWlVNZ0JUVjA3ZUZjeWRmMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvhPgxUT2B+tVxhfnY2AzGyiEmkiQ+XOj+rGPxGMuyhMwSRo63PWIzBSQBoEMtFvml4Ww7NfTscXdsoB5r0O7oBwSE6Ugo0VpiRbpiAiCLkxDSKN/XZZbEqRG5kRs76oMOzdjRfUJCgulKpI11xIJNSCkUDE9HUBhiXu9rGt+4FR6VHagc9dL0KWUzgKx26LNS6ubDxIxh/v+TzxbbrPqKDU9c8JbpAQNJzm1MB7GKxbZXJIAa0k9yurYynWgEgnjJ0oRtKhQfCcLKQFMV5p9YbCu1NTWvbikNzDbKeBqSlhbM8o68HP3pWajm74nBbhBqP9D1DdcpQ9BKBZ8epm98QIDAQABo4IClzCCApMwDgYDVR0PAQH/BAQDAgbAMBUGA1UdJQQOMAwGCisGAQQBgjcKAwwwggFSBgNVHSAEggFJMIIBRTCCAUEGCysGAQQBqHWBBgEBMIIBMDA1BggrBgEFBQcCARYpaHR0cDovL29iLnRydXN0aXMuY29tL3Byb2R1Y3Rpb24vcG9saWNpZXMwgfYGCCsGAQUFBwICMIHpDIHmVGhpcyBDZXJ0aWZpY2F0ZSBpcyBzb2xlbHkgZm9yIHVzZSB3aXRoIE9wZW4gQmFua2luZyBMaW1pdGVkIGFuZCBhc3NvY2lhdGVkIE9wZW4gQmFua2luZyBTZXJ2aWNlcy4gSXRzIHJlY2VpcHQsIHBvc3Nlc3Npb24gb3IgdXNlIGNvbnN0aXR1dGVzIGFjY2VwdGFuY2Ugb2YgdGhlIE9wZW4gQmFua2luZyBMaW1pdGVkIENlcnRpZmljYXRlIFBvbGljeSBhbmQgcmVsYXRlZCBkb2N1bWVudHMgdGhlcmVpbi4wcgYIKwYBBQUHAQEEZjBkMCYGCCsGAQUFBzABhhpodHRwOi8vb2IudHJ1c3Rpcy5jb20vb2NzcDA6BggrBgEFBQcwAoYuaHR0cDovL29iLnRydXN0aXMuY29tL3Byb2R1Y3Rpb24vaXNzdWluZ2NhLmNydDAfBgNVHREEGDAWghRhcGkubmF0aW9ud2lkZS5jby51azA/BgNVHR8EODA2MDSgMqAwhi5odHRwOi8vb2IudHJ1c3Rpcy5jb20vcHJvZHVjdGlvbi9pc3N1aW5nY2EuY3JsMB8GA1UdIwQYMBaAFJ9Jv042p6zDDyvIR/QfKRvAeQsFMB0GA1UdDgQWBBTZ2tk6B4RSfHeCXZ3qwVC52bjEBTANBgkqhkiG9w0BAQsFAAOCAQEAFvt3+1gm3I/xYtG12uWvjLuro7nA5CRSvqRBuMi6aKKWyRMke5n/4B+56h7W8WaSy+YkSw1QQ2ymwLeYsvaYGufPDH7gx5zzJUK2A2Nm4XolDSF4fRFa7V62P7i8+7w3aEGb3T5zF7p8TMygUPxuWXz/g5ac+/MbPIss1nkN019hCEgPwC4Cim72PqiIofA4hf9H68m/+WM8a8R8Bb0m7FZXKv9SUUuOgMsaFXcmm4QYDaxA9H475Oq2lzEdi2WqkO2MWLjr1CPRqJgVnYEtH6tExmaZXbUyE3zaqc62SynoKhsyrU2OQ1dXFAI/X306zadVXmxpfMqUMDX8YBNpmA==\"],\"x5t\":\"R1gGk48-nCfuV0oXlCjYKRX3wJE=\",\"x5t#256\":\"KNyWovW1zmYjUbFRNXOdYGsT39mIHn6_Tw1NfnhloV8=\",\"use\":\"sig\"}]}")
                    .add(
                            "{\"keys\":[{\"kty\":\"RSA\",\"kid\":\"9nqywI9fd7r-3YfHVR-t3u\",\"use\":\"sig\",\"n\":\"jZOfGDdW96U63dpnTc9wZ9-jPdFSDEwHBMPvU5E69R-6L6Yaou0cj-htQiN4z9xPyt20bM-HPkqc3598clW1mPKS_p6D6gxPg0-yeAptl1wef-IJmKwF7t6WXEONpyDiGonUQ6mW8wYHRJIoNIVm-PpEmY9Osw9k0mMgylVC4_BMrBDSXzo-R7ya5qKXZ2Fj5fwziWFAngIQ15l2CMzV55VcINOBsyPsyQH18dusVXLKTFZzj2CIWIyszDJavpGXrrVR_Cj8rqtcU2AMsUo1IxJmp6BOlNnFnvzhfdizfPxtWANnwVKRT2OZbiYC0-_B0MCkA6VgyQUaKFvicyxYfw\",\"e\":\"AQAB\"},{\"kty\":\"EC\",\"kid\":\"WUVrAC-08vooY_GVngf35H\",\"use\":\"sig\",\"x\":\"Yu5_6EsxKr6E3Rqe8zfUYZFJbMNnfMl9PRVby-JF9O8\",\"y\":\"WHAVMnj_T3nRq1NmDklgZ4NoA_BXUERc1KeLKkm3MGo\",\"crv\":\"P-256\"},{\"kty\":\"EC\",\"kid\":\"f4YXohW8Vv9Rv8WS44PqdI\",\"use\":\"sig\",\"x\":\"0Z1pkZ7hGD3iJhRcyhzPUt2f_XUFB3lJuXdThRLPrc_kuYYMzUZTHnG-Ulwqm3il\",\"y\":\"Dw5iR7OPXxweR09pp_Zi-JEUDGXgQk0RDZ5FrSau8DmWrCxz3ll_OU7jx8-Z9xqf\",\"crv\":\"P-384\"},{\"kty\":\"EC\",\"kid\":\"f6g2U-3mYGaUQflSu3e4HE\",\"use\":\"sig\",\"x\":\"AOjlWdNijCE-w3kUp1-BRrt7SgrjRnXxf3i4ST0-ILKu2bDtjWDAAa1b2mZkIe3WUHVpJU91FDIDdBtJ-Lw7U2h_\",\"y\":\"AQ-kM4IAlPrkYmpwFDXZ8K7vi-HwLT3jQiO01l5l6b0lfQlKZsq9xt1CDhFRK82b-rzWEy4dvYn-qDECV92He7vc\",\"crv\":\"P-521\"},{\"kty\":\"EC\",\"kid\":\"Do2mgsOmgsuJNHf6UTZdEt\",\"use\":\"sig\",\"x\":\"AbkqZpw85yVxPLjt6IehmrR_piZ_L1CBmwhKLGyF1e7_RWRGuM4sEXPSSqVSSeapdQ04oljDWfpLq2J1eCYmuQ5z\",\"y\":\"AGHrZ6Veq-wD4NmNWJ-tPuDgotYL_d7PSnfRR6TN979IqarEMWeuP4mS23Gk5O2I0YRdm671SS6dQl3of8nc7SrJ\",\"crv\":\"P-521\"},{\"kty\":\"RSA\",\"kid\":\"d3svD07o_6lZt63zyhABcp\",\"use\":\"sig\",\"n\":\"s37Wb6hk2Khcpa4SWajqOpC1LnYK60mOJAF6sDLOMVriLJWtdp4_wsiN6nQIPeoVysIT0M0H_80NM6kQo04FMCBg7GnMQmJh1bYngW2mOmmFxLzRFTxane-X0z7NL-x8gCnQNGrRsiVXSLf3EdhqkvC0h6uyOTZJLMtD5FUEADTvNSYoq3pfQHHUuDLBct9b40uqPvk4wIbeu0iQY2EaB0t_X1vnunM9rFCdtZK-2eZqQ5iFhCfhi99IFZVQBGs6op9_v6CEqGhashn_aNKKTyUXWRYxbZPkb4ogrtZ1KUyOBy4LPBtSPG3uGDcfRn4c7t-mS4e56xqiSlUqnAFe2w\",\"e\":\"AQAB\"},{\"kty\":\"EC\",\"kid\":\"maZEqOkpl9zbGZS4kJkGFx\",\"use\":\"sig\",\"x\":\"hEc92J-RWY3CLJE3DxaNzylvVTzBoGj2pBmPKKFCvQyuO34LVgfYqVg-kwIzKkg9\",\"y\":\"zdyH_lC63AJ-1sqtoZp550DkrzhbXtazcYDS3apRdL9Ii89I9mi9MLhCcMR5D6zS\",\"crv\":\"P-384\"},{\"kty\":\"EC\",\"kid\":\"ryt9ZUg6Jvu5jk1cP821gr\",\"use\":\"sig\",\"x\":\"F-yvLR3YMNl5nj2teWlHDbyHc8P2WQz5cyBnUkaETkE\",\"y\":\"MVEWs-kydxsi4Z-3DSGgW14xjpt5Epr73hGprh3beqg\",\"crv\":\"P-256\"},{\"kty\":\"EC\",\"kid\":\"-4wz8ovvs-j39ibUC--cdc\",\"use\":\"sig\",\"x\":\"eFVmROOhqFEMR11iAEqdhg917Ni0ypRhsdiKFWla0fwg9cek9fqVm5LBfpekRr2b\",\"y\":\"_05LDu8RiNawuS7YppzC4y6SJEiJTFnXnOAi3unNQ4LrITqXcGJ-y4cvOrfx5-5C\",\"crv\":\"P-384\"},{\"kty\":\"EC\",\"kid\":\"2tBivdS51ItiNeUo5f_FIu\",\"use\":\"sig\",\"x\":\"AE-r5SeRCEzLt9L9Kc2zs7Fx_J9pO9DFvUlnx9Mkdyj4FL8DaTGVKLizqDJpU51CrUx1pdx9YSlaI2hAbyY8yhzE\",\"y\":\"Af4L6HikqlrdD93KCAURIJ7NHy1KPKOvc4ArzSbrk7zWA-PNPP9UscxIE8xuHNSHV7V0z64EQRAxkoIuC2vYl5Qa\",\"crv\":\"P-521\"},{\"kty\":\"RSA\",\"kid\":\"NdmL9TzQTunqM6HpRkfbK0\",\"use\":\"sig\",\"n\":\"ggSiFYtcb9fBIHDPc-FWTRS7jy5ANDH_ceg5cBqS6v3tkIjODwWVSqhZMkjz4mj7SCc98A_HLnUKqXWY7cdf9Fy7sVmNALa_zV-psXHnSKBxVZa2Shafpj-4t5y0bQOYMTwhk_KSNmR__3vVhMdPN9bBPtZmXdT5Y01YJ0b-vyBIGzxz6Pr36nJPLavn7q5WVbgpAFn9llJK5hBigZ4_csHBmq29nIwV65Iskp-UhJbNxP-vFTZenG9hWwFZEE6WubteL3hWvJ6f_QC-ksS4h27wCoeBBU4h4zhqNIObNd2tkNvCpMdTSwQ_GCxdeqyJ_o8ljb4qDPStgj-DLbiKkw\",\"e\":\"AQAB\"},{\"kty\":\"EC\",\"kid\":\"PEOQu2cPpFHEL0aXntZUD5\",\"use\":\"sig\",\"x\":\"7YUdXcsDX7KxpC3pV_n9K2SdIKQOt05our-gwFpzo2A\",\"y\":\"JUgxLExBCawCTSm3KxeQtHGLF38v4jzBGFKtdVarSW0\",\"crv\":\"P-256\"}]}")
                    .build();

    @Test
    public void testJwkSetParsing() {
        JWKS.forEach(
                jwks -> {
                    JsonWebKeySet set =
                            SerializationUtils.deserializeFromString(jwks, JsonWebKeySet.class);
                    List<PublicKey> keys = set.getAllKeys();
                    Assert.assertFalse(keys.isEmpty());
                });
    }
}
