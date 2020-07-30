package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.StatusCode.APPROVED_TAN_PROCEDURES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsRequestProcessor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog.detail.DialogRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype.FinTsAccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.BaseResponsePart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HICAZS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIKAZS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIPINS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIRMS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISALS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISYN;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HITAB;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HITANS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.TanByOperationLookup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.ChosenSecurityFunctionProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.ChosenTanMediumProvider;

public class DialogClient {
    private final FinTsRequestProcessor requestProcessor;
    private final FinTsDialogContext dialogContext;
    private final ChosenTanMediumProvider chosenTanMediumProvider;
    private final DialogRequestBuilder requestBuilder;
    private final FinTsAccountTypeMapper mapper;
    private final ChosenSecurityFunctionProvider chosenSecurityFunctionProvider;

    public DialogClient(
            FinTsRequestProcessor requestProcessor,
            FinTsDialogContext dialogContext,
            ChosenTanMediumProvider chosenTanMediumProvider,
            FinTsAccountTypeMapper mapper,
            ChosenSecurityFunctionProvider chosenSecurityFunctionProvider) {
        this.requestProcessor = requestProcessor;
        this.dialogContext = dialogContext;
        this.chosenTanMediumProvider = chosenTanMediumProvider;
        this.requestBuilder = DialogRequestBuilderProvider.getRequestBuilder(dialogContext);
        this.mapper = mapper;
        this.chosenSecurityFunctionProvider = chosenSecurityFunctionProvider;
    }

    public FinTsResponse initializeSession() {
        dialogContext.resetDialogId();
        FinTsRequest request = requestBuilder.getInitializeSessionRequest(dialogContext);
        FinTsResponse response = requestProcessor.process(request);
        if (!response.isSuccess()) {
            throw new UnsuccessfulApiCallException("Could not initialize session");
        }

        dialogContext.setSystemId(response.findSegmentThrowable(HISYN.class).getSystemId());

        setUpTanByOperationLookup(response);
        extractInformationAboutSupportedOperations(response);
        setUpSecurityFunctionInformation(response);

        return response;
    }

    public void initializeDialog() {
        FinTsRequest request = requestBuilder.getInitRequest(dialogContext);
        FinTsResponse response = requestProcessor.process(request);
        if (!response.isSuccess()) {
            throw new UnsuccessfulApiCallException("Init Request failed");
        }

        setUpAccountsList(response);
        // Parsing HIPINS from this response as well, because sometimes they have different info.
        setUpTanByOperationLookup(response);

        if (dialogContext.isOperationSupported(SegmentType.HKTAB)) {
            response = fetchTanMediumInformation();
            setUpTanMediumInformation(response);
        }
    }

    private void setUpAccountsList(FinTsResponse response) {
        List<FinTsAccountInformation> newAccounts =
                response.findSegments(HIUPD.class).stream()
                        .map(
                                hiupd ->
                                        new FinTsAccountInformation(
                                                hiupd,
                                                mapper.getAccountTypeFor(dialogContext, hiupd)
                                                        .orElse(null)))
                        .collect(Collectors.toList());
        dialogContext.getAccounts().addAll(newAccounts);
    }

    private void setUpTanByOperationLookup(FinTsResponse response) {
        response.findSegment(HIPINS.class)
                .ifPresent(
                        hipins ->
                                dialogContext.setTanByOperationLookup(
                                        new TanByOperationLookup(hipins)));
    }

    private void extractInformationAboutSupportedOperations(FinTsResponse response) {
        List<Pair<SegmentType, Class<? extends BaseResponsePart>>> thingsOfInterest =
                new ArrayList<>();
        thingsOfInterest.add(Pair.of(SegmentType.HKSAL, HISALS.class));
        thingsOfInterest.add(Pair.of(SegmentType.HKKAZ, HIKAZS.class));
        thingsOfInterest.add(Pair.of(SegmentType.HKCAZ, HICAZS.class));
        for (Pair<SegmentType, Class<? extends BaseResponsePart>> p : thingsOfInterest) {
            for (BaseResponsePart segment : response.findSegments(p.getRight())) {
                dialogContext.addOperationSupportedByBank(p.getLeft(), segment);
            }
        }
    }

    private void setUpSecurityFunctionInformation(FinTsResponse response) {
        // information about allowed security functions:
        // https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Security_Sicherheitsverfahren_PINTAN_2018-02-23_final_version.pdf page 53
        dialogContext.setAllowedSecurityFunctions(
                response.findSegmentWithSupportedVersions(HITANS.class)
                        .orElseThrow(IllegalArgumentException::new)
                        .getAllowedScaMethods());
        List<String> allowedTanProcedures =
                response.findSegments(HIRMS.class).stream()
                        .flatMap(
                                hirms ->
                                        hirms.getResponsesWithCode(APPROVED_TAN_PROCEDURES)
                                                .stream())
                        .flatMap(hirmsResponse -> hirmsResponse.getParameters().stream())
                        .collect(Collectors.toList());
        Map<String, String> tanProcedures =
                dialogContext.getAllowedSecurityFunctions().entrySet().stream()
                        .filter(a -> allowedTanProcedures.contains(a.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        dialogContext.setAllowedSecurityFunctions(tanProcedures);

        Optional.ofNullable(chosenSecurityFunctionProvider.getChosenSecurityFunction(dialogContext))
                .ifPresent(dialogContext::setChosenSecurityFunction);
    }

    private FinTsResponse fetchTanMediumInformation() {
        FinTsRequest request = requestBuilder.getTanMedium(dialogContext);
        FinTsResponse response = requestProcessor.process(request);
        if (!response.isSuccess()) {
            throw new UnsuccessfulApiCallException("Getting TAN Medium failed");
        }
        return response;
    }

    private void setUpTanMediumInformation(FinTsResponse response) {
        HITAB segment = response.findSegmentThrowable(HITAB.class);
        List<String> tanMediumList = dialogContext.getTanMediumList();
        for (HITAB.TanMedia tanMedium : segment.getTanMediaList()) {
            tanMediumList.add(tanMedium.getTanMediumName());
        }
        dialogContext.setChosenTanMedium(chosenTanMediumProvider.getTanMedium(dialogContext));
    }

    public FinTsResponse finish() {
        FinTsRequest request = requestBuilder.getFinishRequest(dialogContext);
        FinTsResponse response = requestProcessor.process(request);
        if (!response.isSuccess()) {
            throw new UnsuccessfulApiCallException("Could not end dialog properly");
        }
        dialogContext.setDialogId("0");
        dialogContext.setMessageNumber(1);

        return response;
    }
}
