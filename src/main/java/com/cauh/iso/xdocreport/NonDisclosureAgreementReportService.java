package com.cauh.iso.xdocreport;


import com.cauh.common.utils.Base64Utils;
import com.cauh.iso.component.DocumentAssembly;
import com.cauh.iso.domain.NonDisclosureAgreement;
import com.cauh.iso.domain.report.SOPDisclosureRequestForm;
import com.cauh.iso.utils.DateUtils;
import com.cauh.iso.xdocreport.dto.NonDisclosureAgreementDTO;
import com.groupdocs.assembly.DataSourceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class NonDisclosureAgreementReportService {
    private final DocumentAssembly documentAssembly;
//    @Value("${file.upload-dir}")
//    private String fileUploadDir;
//    private InputStream in = TrainingLogReportService.class.getResourceAsStream("SOP_Non_Disclosure_Agreement.docx");
//    private IXDocReport report;



    public void generateReport(NonDisclosureAgreement agreement, HttpServletResponse response) {
        try(OutputStream os = response.getOutputStream()) {
            InputStream in = TrainingLogReportService.class.getResourceAsStream("Non-Disclosure_Agreement_for_SOP_01.docx");

            SOPDisclosureRequestForm sopDisclosureRequestForm = agreement.getExternalCustomer().getSopDisclosureRequestForm();
            StringBuilder roleAndCompany = new StringBuilder();
            if(!StringUtils.isEmpty(agreement.getExternalCustomer().getRole())) {
                roleAndCompany.append(agreement.getExternalCustomer().getRole());
            }

            if(roleAndCompany.length() != 0 && StringUtils.isEmpty(sopDisclosureRequestForm.getCompanyNameOrInstituteName()) == false) {
                roleAndCompany.append("/").append(sopDisclosureRequestForm.getCompanyNameOrInstituteName());
            }

            NonDisclosureAgreementDTO dto = new NonDisclosureAgreementDTO();
            dto.setCustomerName(agreement.getExternalCustomer().getName());
            dto.setRoleAndCompany(roleAndCompany.toString());
            dto.setSign(new ByteArrayInputStream(Base64Utils.decodeBase64ToBytes(agreement.getBase64signature())));
            dto.setPurpose(sopDisclosureRequestForm.getPurposeOfDisclosure().name());
            dto.setPurposeOther(sopDisclosureRequestForm.getPurposeOfDisclosureOther());
            dto.setAgreementDate(DateUtils.format(agreement.getCreatedDate(), "dd-MMM-yyyy").toUpperCase());

            DataSourceInfo dataSourceInfo = new DataSourceInfo(dto, "");
            documentAssembly.assembleDocumentAsPdf(in, os, dataSourceInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

