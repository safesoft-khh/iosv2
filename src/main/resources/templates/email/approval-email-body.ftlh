<#setting locale="en_US">
<!-- 1 SOP Deviation Report -->
<#if approval.sopDeviationReport?has_content>
    <table class="invoice-items" style="margin: 25px auto; text-align: left; width: 100%;" cellpadding="0" cellspacing="0">
        <tr>
            <td style="padding: 5px 0;">Project No.</td>
            <td class="alignleft" style="padding: 5px 0; text-align: left;"><#if approval.sopDeviationReport.projectNo?has_content>${approval.sopDeviationReport.projectNo}</#if></td>
        </tr>
        <tr>
            <td>Deviated SOP</td>
            <td class="alignleft" style="padding: 5px 0; text-align: left;">
                <#if deviatedSOPDocument?has_content && deviatedSOPDocument.document?has_content>
                    ${deviatedSOPDocument.document.docId} & ${deviatedSOPDocument.document.title} v${deviatedSOPDocument.version}
                </#if>
            </td>
        </tr>
        <tr>
            <td style="padding: 5px 0;">Confirmation Date of SOP<br>Training Deviation</td>
            <td class="alignleft" style="padding: 5px 0; text-align: left;"><#if approval.sopDeviationReport.confirmationDate?has_content>${approval.sopDeviationReport.confirmationDate?string('dd-MMM-yyyy')?upper_case}</#if></td>
        </tr>
    </table>
    <#elseif approval.sopRfRequestForm?has_content>
        <!-- 2 SOP/RD Request Form -->
        <table class="invoice-items" cellpadding="0" cellspacing="0">
            <tr>
                <td>Request for</td>
                <td class="alignleft">
                    <#if approval.sopRfRequestForm.newSOPDevelopment == true && approval.sopRfRequestForm.sopDevelopmentDocs?has_content>
                        <strong>[New SOP Development]</strong><br/>
                        <#list approval.sopRfRequestForm.sopDevelopmentDocs as sop>
                            - ${sop.docId}/${sop.title}/v${sop.version}<br/>
                        </#list>
                        <br/>
                    </#if>
                    <#if approval.sopRfRequestForm.newRDDevelopment == true && approval.sopRfRequestForm.rdDevelopmentDocs?has_content>
                        <strong>[New RD Development]</strong><br/>
                        <#list approval.sopRfRequestForm.rdDevelopmentDocs as rd>
                            - ${rd.docId}/${rd.title}/v${rd.version}<br/>
                        </#list>
                        <br/>
                    </#if>
                    <#if sopRevisionDocs?has_content>
                        <strong>[SOP Revision]</strong><br/>
                        <#list sopRevisionDocs as sop>
                            - ${sop.document.docId}/${sop.document.title}/v${sop.version}<br/>
                        </#list>
                        <br/>
                    </#if>
                    <#if rdRevisionDocs?has_content>
                        <strong>[RD Revision]</strong><br/>
                        <#list rdRevisionDocs as rd>
                            - ${rd.document.docId}/${rd.document.title}/v${rd.version}<br/>
                        </#list>
                    </#if>
                </td>
            </tr>
            <tr>
                <td>Reason for the request</td>
                <td class="alignleft">${approval.sopRfRequestForm.reasonForTheRequest}</td>
            </tr>
        </table>
    <#elseif approval.sopWaiverApprovalForm?has_content>
    <!-- 3 SOP Waiver Request and Approval Form -->
    <table class="invoice-items" cellpadding="0" cellspacing="0">
        <tr>
            <td>Project No.</td>
            <td class="alignleft"><#if approval.sopWaiverApprovalForm.projectNo?has_content>${approval.sopWaiverApprovalForm.projectNo}</#if></td>
        </tr>
        <tr>
            <td>Protocol Title/No.</td>
            <td class="alignleft"><#if approval.sopWaiverApprovalForm.protocolNo?has_content>${approval.sopWaiverApprovalForm.protocolNo}</#if></td>
        </tr>
        <tr>
            <td>Waiver Start Date</td>
            <td class="alignleft">${approval.sopWaiverApprovalForm.waiverStartDate?string('dd-MMM-yyyy')?upper_case}</td>
        </tr>
        <tr>
            <td>Waiver End Date</td>
            <td class="alignleft">${approval.sopWaiverApprovalForm.waiverEndDate?string('dd-MMM-yyyy')?upper_case}</td>
        </tr>
        <tr>
            <td>Waived SOP</td>
            <td class="alignleft">${deviatedSOP.document.docId}/${deviatedSOP.document.title}/v${deviatedSOP.version}</td>
        </tr>
    </table>
    <#elseif approval.sopDisclosureRequestForm?has_content>
    <table class="invoice-items" style="margin: 25px auto; text-align: left; width: 100%;" cellpadding="0" cellspacing="0">
<#--        <tr>-->
<#--            <td>Protocol Title/No.</td>-->
<#--            <td class="alignleft">${approval.sopDisclosureRequestForm.protocolTitleOrNo?default('')}</td>-->
<#--        </tr>-->
        <tr>
            <td style="padding: 5px 0;">Company Name or Institute Name</td>
            <td class="alignleft" style="padding: 5px 0; text-align: left;">${approval.sopDisclosureRequestForm.companyNameOrInstituteName?default('')}</td>
        </tr>
        <tr>
            <td style="padding: 5px 0;">Request Date</td>
            <td class="alignleft" style="padding: 5px 0; text-align: left;">${approval.sopDisclosureRequestForm.requestStartDate?string('dd-MMM-yyyy')?upper_case} ~ ${approval.sopDisclosureRequestForm.requestEndDate?string('dd-MMM-yyyy')?upper_case}</td>
        </tr>
        <tr>
            <td style="padding: 5px 0;">Purpose of Disclosure</td>
            <td class="alignleft" style="padding: 5px 0; text-align: left;">${approval.sopDisclosureRequestForm.purposeOfDisclosure.label}
                <#if approval.sopDisclosureRequestForm.purposeOfDisclosure == 'OTHER'>
                    : <u>${approval.sopDisclosureRequestForm.purposeOfDisclosureOther}</u>
                </#if></td>
        </tr>
    </table>
    <#elseif approval.retirementApprovalForm?has_content>
    <!-- 5 SOP/RD Retirement Form -->
    <h5>[Request for Retirement Documents]</h5>
    <table class="invoice-items" cellpadding="0" cellspacing="0">
        <tr>
            <th class="aligncenter">Document Type</th>
            <th class="aligncenter">SOP/${formName}<br/>Document Id</th>
            <th class="aligncenter">SOP/${formName}<br/>Title</th>
            <th class="aligncenter">Ver.</th>
            <th class="aligncenter">Effective Date</th>
        </tr>
        <#if retirementSOPs?has_content>
            <#list retirementSOPs as sop>
        <tr>
            <td class="aligncenter">SOP</td>
            <td class="aligncenter">${sop.document.docId}</td>
            <td class="aligncenter">${sop.document.title}</td>
            <td class="aligncenter">${sop.version}</td>
            <td class="aligncenter">${sop.effectiveDate?string('dd-MMM-yyyy')?upper_case}</td>
        </tr>
            </#list>
        </#if>
        <#if retirementRDs?has_content>
            <#list retirementRDs as rd>
                <tr>
                    <td class="aligncenter">${formName}</td>
                    <td class="aligncenter">${rd.document.docId}</td>
                    <td class="aligncenter">${rd.document.title}</td>
                    <td class="aligncenter">${rd.version}</td>
                    <td class="aligncenter">${rd.effectiveDate?string('dd-MMM-yyyy')?upper_case}</td>
                </tr>
            </#list>
        </#if>
    </table>
</#if>


