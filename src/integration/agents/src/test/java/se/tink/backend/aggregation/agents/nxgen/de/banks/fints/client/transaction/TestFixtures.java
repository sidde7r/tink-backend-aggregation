package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsSecretsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.BaseResponsePart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HICAZS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIPINS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISPA;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.TanByOperationLookup;

@Ignore
public class TestFixtures {

    private TestFixtures() {}

    static FinTsAccountInformation getAccountInformation() {
        HIUPD hiupd =
                new HIUPD()
                        .setAccountType(1)
                        .setIban("123456789")
                        .setAccountNumber("123456789")
                        .setBlz("12312")
                        .setProductName("Girokonto")
                        .setFirstAccountHolder("First Holder");

        HISPA.Detail hispaDetail = new HISPA.Detail().setIban("123456789").setBic("123123123");

        return new FinTsAccountInformation(hiupd).setSepaDetails(hispaDetail);
    }

    static String getBodyOfFetchTransactionsResponseInMT940Format() {
        return "SE5IQks6MTozKzAwMDAwMDAwMDk4MiszMDArMzc2MzAyNzE3MDcwNDAwMEREa3RFWUpVTXAuQTEx"
                + "KzUrMzc2MzAyNzE3MDcwNDAwMEREa3RFWUpVTXAuQTExOjUnSE5WU0s6OTk4OjMrUElOOjErOTk"
                + "4KzErMjo6Mzc2MzAyNzE3MDM1OTAwMElIRzVIQlIyT0NIMldMKzE6MjAyMDAzMzA6MTUxOTM2Kz"
                + "I6MjoxMzpAOEAAAAAAAAAAADo1OjErMjgwOjEwMDEwMDEwOmphbmdpbGxpY2g6VjowOjArMCdIT"
                + "lZTRDo5OTk6MStANzI4QEhJUk1HOjI6MiswMDEwOjpOYWNocmljaHQgZW50Z2VnZW5nZW5vbW1l"
                + "bi4nSElSTVM6MzoyOjMrMDAyMDo6QXVmdHJhZyBhdXNnZWb8aHJ0LidISUtBWjo0OjU6MytANjI"
                + "4QA0KOjIwOlNUQVJUVU1TDQo6MjE6Tk9OUkVGDQo6MjU6MTAwMTAwMTAvNTU0NDgxMTI3DQo6Mj"
                + "hDOjANCjo2MEY6QzE5MTIzMUVVUjAsNTANCjo2MToyMDAxMzAwMTMwQzEsMDBOMDUyTk9OUkVGD"
                + "Qo6ODY6MTUyPzAwRCBHVVQgU0VQQT8yMFJlZmVyZW56IE5PVFBST1ZJREVEPzIxVmVyd2VuZHVu"
                + "Z3N6d2Vjaz8yMlRvIG8NCnduIGFjY291bnQ/MzBHRU5PREVGMU0wMz8zMURFMzI3MDE2OTQ2NjA"
                + "wMDA5NzkxNjM/MzJKYW4gR2lsbGljaD8zDQo0MDAwDQo6NjE6MjAwMzAyMDMwMkMxLDAwTjA1Mk"
                + "5PTlJFRg0KOjg2OjE1Mj8wMEQgR1VUIFNFUEE/MjBSZWZlcmVueiBOT1RQUk9WSURFRD8yMVZlc"
                + "ndlbmR1bmdzendlY2s/MjJUbyBvDQp3biBhY2NvdW50PzMwR0VOT0RFRjFNMDM/MzFERTMyNzAx"
                + "Njk0NjYwMDAwOTc5MTYzPzMySmFuIEdpbGxpY2g/Mw0KNDAwMA0KOjYxOjIwMDMzMDAzMzBDMSw"
                + "wME4wNTJOT05SRUYNCjo4NjoxNTI/MDBEIEdVVCBTRVBBPzIwUmVmZXJlbnogTk9UUFJPVklERU"
                + "Q/MjFWZXJ3ZW5kdW5nc3p3ZWNrPzIyVG8gbw0Kd24gYWNjb3VudD8zMEdFTk9ERUYxTTAzPzMxR"
                + "EUzMjcwMTY5NDY2MDAwMDk3OTE2Mz8zMkphbiBHaWxsaWNoPzMNCjQwMDANCjo2MkY6QzIwMDMz"
                + "MEVVUjMsNTANCi0nJ0hOSEJTOjU6MSs1Jw==";
    }

    static String getBodyOfFetchTransactionsResponseInXMLFormat() {
        return "SE5IQks6MTozKzAwMDAwMDAwNDU3NCszMDArSUQwMDMzMDE2MzIyMTEzKzUrSUQwMDMzMDE2MzIy"
                + "MTEzOjUnSE5WU0s6OTk4OjMrUElOOjIrOTk4KzErMjo6enNnY0pTN0hLM0VCQUFEck9QY2ZoMj8r"
                + "b3dBUUErMToyMDIwMDMzMDoxNjMzMzYrMjoyOjEzOkA4QDAwMDAwMDAwOjU6MSsyODA6NzAxNjk0"
                + "NjY6MTE5NTQ2NzY5OlM6MDowKzAnSE5WU0Q6OTk5OjErQDQzNTFASE5TSEs6Mjo0K1BJTjoyKzk0"
                + "NCs4MDYzNDA4KzErMSsyOjp6c2djSlM3SEszRUJBQURyT1BjZmgyPytvd0FRQSsxKzE6MjAyMDAz"
                + "MzA6MTYzMzM2KzE6OTk5OjErNjoxMDoxNisyODA6NzAxNjk0NjY6MTE5NTQ2NzY5OlM6MDowJ0hJ"
                + "Uk1HOjM6MiswMDEwOjpOYWNocmljaHQgZW50Z2VnZW5nZW5vbW1lbi4nSElSTVM6NDoyOjMrMDAy"
                + "MDo6KkFiZnJhZ2UgQ0FNVCBVbXPkdHplIGVyZm9sZ3JlaWNoIGR1cmNoZ2Vm/GhydCswOTAwOjoq"
                + "VEFOIGVudHdlcnRldC4nSElUQU46NTo2OjMrMisrUXc0ZEpTN0hLM0VCQUFEck9QY2ZoMj8rb3dB"
                + "UUEnSElDQVo6NjoxOjMrREUzMjcwMTY5NDY2MDAwMDk3OTE2MzpHRU5PREVGMU0wMzo5NzkxNjM6"
                + "OjI4MDo3MDE2OTQ2Nit1cm4/Omlzbz86c3RkPzppc28/OjIwMDIyPzp0ZWNoPzp4c2Q/OmNhbXQu"
                + "MDUyLjAwMS4wMitAMzg5OEA8P3htbCB2ZXJzaW9uPSIxLjAiIGVuY29kaW5nPSJJU08tODg1OS0x"
                + "IiA/PjxEb2N1bWVudCB4bWxucz0idXJuOmlzbzpzdGQ6aXNvOjIwMDIyOnRlY2g6eHNkOmNhbXQu"
                + "MDUyLjAwMS4wMiIgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1p"
                + "bnN0YW5jZSIgeHNpOnNjaGVtYUxvY2F0aW9uPSJ1cm46aXNvOnN0ZDppc286MjAwMjI6dGVjaDp4"
                + "c2Q6Y2FtdC4wNTIuMDAxLjAyIGNhbXQuMDUyLjAwMS4wMi54c2QiPjxCa1RvQ3N0bXJBY2N0UnB0"
                + "PjxHcnBIZHI+PE1zZ0lkPjA1MkQyMDIwLTAzLTMwVDE2OjMzOjM2LjBOMDAwMDAwMDAwPC9Nc2dJ"
                + "ZD48Q3JlRHRUbT4yMDIwLTAzLTMwVDE2OjMzOjM2LjArMDI6MDA8L0NyZUR0VG0+PE1zZ1BnbnRu"
                + "PjxQZ05iPjE8L1BnTmI+PExhc3RQZ0luZD50cnVlPC9MYXN0UGdJbmQ+PC9Nc2dQZ250bj48L0dy"
                + "cEhkcj48UnB0PjxJZD4zMTcxQzUyMjAyMDAzMzAxNjMzMzY8L0lkPjxFbGN0cm5jU2VxTmI+MDAw"
                + "MDAwMDAwPC9FbGN0cm5jU2VxTmI+PENyZUR0VG0+MjAyMC0wMy0zMFQxNjozMzozNi4wKzAyOjAw"
                + "PC9DcmVEdFRtPjxBY2N0PjxJZD48SUJBTj5ERTMyNzAxNjk0NjYwMDAwOTc5MTYzPC9JQkFOPjwv"
                + "SWQ+PENjeT5FVVI8L0NjeT48T3ducj48Tm0+SmFuIEdpbGxpY2g8L05tPjwvT3ducj48U3Zjcj48"
                + "RmluSW5zdG5JZD48QklDPkdFTk9ERUYxTTAzPC9CSUM+PE5tPlJhaWZmZWlzZW5iYW5rPC9ObT48"
                + "T3Rocj48SWQ+REUgMTI5NTExODM4PC9JZD48SXNzcj5VbXNTdElkPC9Jc3NyPjwvT3Rocj48L0Zp"
                + "bkluc3RuSWQ+PC9TdmNyPjwvQWNjdD48QmFsPjxUcD48Q2RPclBydHJ5PjxDZD5QUkNEPC9DZD48"
                + "L0NkT3JQcnRyeT48L1RwPjxBbXQgQ2N5PSJFVVIiPjI3Ny41NjwvQW10PjxDZHREYnRJbmQ+Q1JE"
                + "VDwvQ2R0RGJ0SW5kPjxEdD48RHQ+MjAyMC0wMS0zMDwvRHQ+PC9EdD48L0JhbD48QmFsPjxUcD48"
                + "Q2RPclBydHJ5PjxDZD5DTEJEPC9DZD48L0NkT3JQcnRyeT48L1RwPjxBbXQgQ2N5PSJFVVIiPjI3"
                + "NC41NjwvQW10PjxDZHREYnRJbmQ+Q1JEVDwvQ2R0RGJ0SW5kPjxEdD48RHQ+MjAyMC0wMy0zMDwv"
                + "RHQ+PC9EdD48L0JhbD48TnRyeT48QW10IENjeT0iRVVSIj4xLjAwPC9BbXQ+PENkdERidEluZD5E"
                + "QklUPC9DZHREYnRJbmQ+PFN0cz5CT09LPC9TdHM+PEJvb2tnRHQ+PER0PjIwMjAtMDEtMzA8L0R0"
                + "PjwvQm9va2dEdD48VmFsRHQ+PER0PjIwMjAtMDEtMzA8L0R0PjwvVmFsRHQ+PEFjY3RTdmNyUmVm"
                + "PjIwMjAwMTMwMDA1MzMwNTIwMDA8L0FjY3RTdmNyUmVmPjxCa1R4Q2QvPjxOdHJ5RHRscz48VHhE"
                + "dGxzPjxSZWZzPjxNc2dJZD54elFSVHd4akFzY0JMVWxaQXhjPC9Nc2dJZD48UG10SW5mSWQ+MDAw"
                + "MDk3OTE2My8wMDAwMDAwMDEvICBWMDAwMDE8L1BtdEluZklkPjxFbmRUb0VuZElkPk5PVFBST1ZJ"
                + "REVEPC9FbmRUb0VuZElkPjwvUmVmcz48QmtUeENkPjxQcnRyeT48Q2Q+TlRSRisxMTcrMDA5MDA8"
                + "L0NkPjxJc3NyPlpLQTwvSXNzcj48L1BydHJ5PjwvQmtUeENkPjxSbHRkUHRpZXM+PERidHI+PE5t"
                + "PkphbiBHaWxsaWNoPC9ObT48L0RidHI+PERidHJBY2N0PjxJZD48SUJBTj5ERTMyNzAxNjk0NjYw"
                + "MDAwOTc5MTYzPC9JQkFOPjwvSWQ+PC9EYnRyQWNjdD48Q2R0cj48Tm0+SmFuIEdpbGxpY2g8L05t"
                + "PjwvQ2R0cj48Q2R0ckFjY3Q+PElkPjxJQkFOPkRFMTMxMDAxMDAxMDA1NTQ0ODExMjc8L0lCQU4+"
                + "PC9JZD48L0NkdHJBY2N0PjwvUmx0ZFB0aWVzPjxSbHRkQWd0cz48Q2R0ckFndD48RmluSW5zdG5J"
                + "ZD48QklDPlBCTktERUZGWFhYPC9CSUM+PC9GaW5JbnN0bklkPjwvQ2R0ckFndD48L1JsdGRBZ3Rz"
                + "PjxQdXJwPjxDZD5SSU5QPC9DZD48L1B1cnA+PFJtdEluZj48VXN0cmQ+VG8gb3duIGFjY291bnQg"
                + "SUJBTjogREUxMzEwMDEwMDEwMDU1NDQ4MTEyNyBCSUM6IFBCTktERUZGWFhYPC9Vc3RyZD48L1Jt"
                + "dEluZj48L1R4RHRscz48L050cnlEdGxzPjxBZGR0bE50cnlJbmY+REFVRVJBVUZUUkFHICAgICAg"
                + "ICA8L0FkZHRsTnRyeUluZj48L050cnk+PE50cnk+PEFtdCBDY3k9IkVVUiI+MS4wMDwvQW10PjxD"
                + "ZHREYnRJbmQ+REJJVDwvQ2R0RGJ0SW5kPjxTdHM+Qk9PSzwvU3RzPjxCb29rZ0R0PjxEdD4yMDIw"
                + "LTAzLTAyPC9EdD48L0Jvb2tnRHQ+PFZhbER0PjxEdD4yMDIwLTAzLTAyPC9EdD48L1ZhbER0PjxB"
                + "Y2N0U3ZjclJlZj4yMDIwMDMwMjAzNTMwMTI3MDAwPC9BY2N0U3ZjclJlZj48QmtUeENkLz48TnRy"
                + "eUR0bHM+PFR4RHRscz48UmVmcz48TXNnSWQ+eHlKSWZ3eGpBc2VkbkJ0ZlcybyAgICAgICAgIDAw"
                + "MDAwMDU8L01zZ0lkPjxQbXRJbmZJZD4wMDAwOTc5MTYzLzAwMDAwMDAwMS8gIFYwMDAwMTwvUG10"
                + "SW5mSWQ+PEVuZFRvRW5kSWQ+Tk9UUFJPVklERUQ8L0VuZFRvRW5kSWQ+PC9SZWZzPjxCa1R4Q2Q+"
                + "PFBydHJ5PjxDZD5OVFJGKzExNyswMDkwMDwvQ2Q+PElzc3I+WktBPC9Jc3NyPjwvUHJ0cnk+PC9C"
                + "a1R4Q2Q+PFJsdGRQdGllcz48RGJ0cj48Tm0+SmFuIEdpbGxpY2g8L05tPjwvRGJ0cj48RGJ0ckFj"
                + "Y3Q+PElkPjxJQkFOPkRFMzI3MDE2OTQ2NjAwMDA5NzkxNjM8L0lCQU4+PC9JZD48L0RidHJBY2N0"
                + "PjxDZHRyPjxObT5KYW4gR2lsbGljaDwvTm0+PC9DZHRyPjxDZHRyQWNjdD48SWQ+PElCQU4+REUx"
                + "MzEwMDEwMDEwMDU1NDQ4MTEyNzwvSUJBTj48L0lkPjwvQ2R0ckFjY3Q+PC9SbHRkUHRpZXM+PFJs"
                + "dGRBZ3RzPjxDZHRyQWd0PjxGaW5JbnN0bklkPjxCSUM+UEJOS0RFRkZYWFg8L0JJQz48L0Zpbklu"
                + "c3RuSWQ+PC9DZHRyQWd0PjwvUmx0ZEFndHM+PFB1cnA+PENkPlJJTlA8L0NkPjwvUHVycD48Um10"
                + "SW5mPjxVc3RyZD5UbyBvd24gYWNjb3VudCBJQkFOOiBERTEzMTAwMTAwMTAwNTU0NDgxMTI3IEJJ"
                + "QzogUEJOS0RFRkZYWFg8L1VzdHJkPjwvUm10SW5mPjwvVHhEdGxzPjwvTnRyeUR0bHM+PEFkZHRs"
                + "TnRyeUluZj5EQVVFUkFVRlRSQUcgICAgICAgIDwvQWRkdGxOdHJ5SW5mPjwvTnRyeT48TnRyeT48"
                + "QW10IENjeT0iRVVSIj4xLjAwPC9BbXQ+PENkdERidEluZD5EQklUPC9DZHREYnRJbmQ+PFN0cz5C"
                + "T09LPC9TdHM+PEJvb2tnRHQ+PER0PjIwMjAtMDMtMzA8L0R0PjwvQm9va2dEdD48VmFsRHQ+PER0"
                + "PjIwMjAtMDMtMzA8L0R0PjwvVmFsRHQ+PEFjY3RTdmNyUmVmPjIwMjAwMzMwMDA1MjE5NjQwMDA8"
                + "L0FjY3RTdmNyUmVmPjxCa1R4Q2QvPjxOdHJ5RHRscz48VHhEdGxzPjxSZWZzPjxNc2dJZD54d0FW"
                + "RHd4akFzZTNFMUprM0xVPC9Nc2dJZD48UG10SW5mSWQ+MDAwMDk3OTE2My8wMDAwMDAwMDEvICBW"
                + "MDAwMDE8L1BtdEluZklkPjxFbmRUb0VuZElkPk5PVFBST1ZJREVEPC9FbmRUb0VuZElkPjwvUmVm"
                + "cz48QmtUeENkPjxQcnRyeT48Q2Q+TlRSRisxMTcrMDA5MDA8L0NkPjxJc3NyPlpLQTwvSXNzcj48"
                + "L1BydHJ5PjwvQmtUeENkPjxSbHRkUHRpZXM+PERidHI+PE5tPkphbiBHaWxsaWNoPC9ObT48L0Ri"
                + "dHI+PERidHJBY2N0PjxJZD48SUJBTj5ERTMyNzAxNjk0NjYwMDAwOTc5MTYzPC9JQkFOPjwvSWQ+"
                + "PC9EYnRyQWNjdD48Q2R0cj48Tm0+SmFuIEdpbGxpY2g8L05tPjwvQ2R0cj48Q2R0ckFjY3Q+PElk"
                + "PjxJQkFOPkRFMTMxMDAxMDAxMDA1NTQ0ODExMjc8L0lCQU4+PC9JZD48L0NkdHJBY2N0PjwvUmx0"
                + "ZFB0aWVzPjxSbHRkQWd0cz48Q2R0ckFndD48RmluSW5zdG5JZD48QklDPlBCTktERUZGWFhYPC9C"
                + "SUM+PC9GaW5JbnN0bklkPjwvQ2R0ckFndD48L1JsdGRBZ3RzPjxQdXJwPjxDZD5SSU5QPC9DZD48"
                + "L1B1cnA+PFJtdEluZj48VXN0cmQ+VG8gb3duIGFjY291bnQgSUJBTjogREUxMzEwMDEwMDEwMDU1"
                + "NDQ4MTEyNyBCSUM6IFBCTktERUZGWFhYPC9Vc3RyZD48L1JtdEluZj48L1R4RHRscz48L050cnlE"
                + "dGxzPjxBZGR0bE50cnlJbmY+REFVRVJBVUZUUkFHICAgICAgICA8L0FkZHRsTnRyeUluZj48L050"
                + "cnk+PC9ScHQ+PC9Ca1RvQ3N0bXJBY2N0UnB0PjwvRG9jdW1lbnQ+J0hOU0hBOjc6Mis4MDYzNDA4"
                + "JydITkhCUzo4OjErNSc=";
    }

    static String getBodyOfUnsuccessfulResponse() {
        return "SE5IQks6MTozKzAwMDAwMDAwMDMzMyszMDArMzc2MjQxNDQwNjI0NzAwMDU5TCpYcU9xZjIuRDExKzU"
                + "rMzc2MjQxNDQwNjI0NzAwMDU5TCpYcU9xZjIuRDExOjUnSE5WU0s6OTk4OjMrUElOOjErOTk4KzErM"
                + "jo6Mzc2MjQxNDQwNTk3OTAwMEpITUhIQzVRM1U5MVZKKzE6MjAyMDAzMjM6MTIwNjU1KzI6MjoxMzp"
                + "AOEAAAAAAAAAAADo1OjErMjgwOjEwMDEwMDEwOmphbmdpbGxpY2g6VjowOjArMCdITlZTRDo5OTk6M"
                + "StAODBASElSTUc6MjoyKzkwNTA6OlRlaWx3ZWlzZSBmZWhsZXJoYWZ0LidISVJNUzozOjI6Mys5MjE"
                + "wOjpXaXJkIG5pY2h0IHVudGVyc3T8dHp0LicnSE5IQlM6NDoxKzUn";
    }

    static FinTsConfiguration getFinTsConfiguration(final int port) {
        final String socket = String.format("http://localhost:%d/foo/bar", port);
        return new FinTsConfiguration("foo", Bank.POSTBANK, socket, "foo", "foo");
    }

    static FinTsDialogContext getDialogContext(FinTsConfiguration configuration) {
        FinTsDialogContext context =
                new FinTsDialogContext(configuration, new FinTsSecretsConfiguration(null, null));
        BaseResponsePart hksal = mock(BaseResponsePart.class);
        when(hksal.getSegmentVersion()).thenReturn(5);
        context.addOperationSupportedByBank(SegmentType.HKSAL, hksal);
        HICAZS hicazs =
                new HICAZS()
                        .setSupportedCamtFormats(
                                Collections.singletonList(
                                        "urn:iso:std:iso:20022:tech:xsd:camt.052.001.02"));
        hicazs.setSegmentVersion(1);
        context.addOperationSupportedByBank(SegmentType.HKCAZ, hicazs);
        context.setTanByOperationLookup(getOperationLookup());
        return context;
    }

    private static TanByOperationLookup getOperationLookup() {
        HIPINS hipins =
                new HIPINS()
                        .setOperations(
                                Arrays.asList(
                                        Pair.of(SegmentType.HKSPA.getSegmentName(), false),
                                        Pair.of(SegmentType.HKSAL.getSegmentName(), false)));
        return new TanByOperationLookup(hipins);
    }
}
