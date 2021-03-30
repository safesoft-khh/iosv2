<#import "/spring.ftl" as spring />
<#--<input type="hidden" name="_method" value="put"/>-->
<table class="table table-bordered">
    <thead>
    <tr class="bg-gray-light">
        <th class="text-center">Document Type</th>
        <th class="text-center">SOP/${formName}}<br/>Document Id</th>
        <th>SOP/RD Title</th>
        <th class="text-center">Ver.</th>
        <th class="text-center">Effective Date</th>
    </tr>
    </thead>
    <tbody>
    <#if approvalView.retirementApprovalForm.retirementDocumentSOPs?has_content>
        <#list approvalView.retirementApprovalForm.retirementDocumentSOPs as v>
            <tr>
                <td class="text-center">${v.documentVersion.document.type.name()}</td>
                <td class="text-center">${v.documentVersion.document.docId}</td>
                <td>${v.documentVersion.document.title}</td>
                <td class="text-center">${v.documentVersion.version}</td>
                <td class="text-center">${v.documentVersion.effectiveDate?string('dd-MMM-yyyy')}</td>
            </tr>
        </#list>
    </#if>
    <#if approvalView.retirementApprovalForm.retirementDocumentRDs?has_content>
        <#list approvalView.retirementApprovalForm.retirementDocumentRDs as v>
            <tr>
                <td class="text-center">${v.documentVersion.document.type.name()}</td>
                <td class="text-center">${v.documentVersion.document.docId}</td>
                <td>${v.documentVersion.document.title}</td>
                <td class="text-center">${v.documentVersion.version}</td>
                <td class="text-center">${v.documentVersion.effectiveDate?string('dd-MMM-yyyy')}</td>
            </tr>
        </#list>
    </#if>
    </tbody>
</table>

<table class="table table-bordered">
    <tbody>
    <tr>
        <td class="bg-gray-light col-xs-3 text-bold">Reason</td>
        <td>
            ${approvalView.retirementApprovalForm.reason?default('')?replace('\n', '<br/>')?no_esc}
        </td>
    </tr>
<#--    <tr>-->
<#--        <td class="bg-gray-light text-bold">Retirement Date</td>-->
<#--        <td>-->
<#--            ${approvalView.retirementApprovalForm.retirementDate?string('dd-MMM-yyyy')}-->
<#--        </td>-->
<#--    </tr>-->
    </tbody>
</table>