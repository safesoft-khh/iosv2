<h5>Requester Information</h5>
<table class="table table-bordered">
    <tbody>
    <tr>
        <td class="bg-gray-light col-xs-3">Name of Requester</td>
        <td>
            ${approvalView.sopDisclosureRequestForm.nameOfRequester!''}
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Team/Department</td>
        <td>
            ${approvalView.sopDisclosureRequestForm.teamDept!''}
        </td>
    </tr>
<#--    <tr>-->
<#--        <td class="bg-gray-light">Protocol Title or No.</td>-->
<#--        <td>-->
<#--            ${approvalView.sopDisclosureRequestForm.protocolTitleOrNo!''}-->
<#--        </td>-->
<#--    </tr>-->
<#--    <tr>-->
<#--        <td class="bg-gray-light">Project Manager or Team Manager</td>-->
<#--        <td>-->
<#--            ${approvalView.sopDisclosureRequestForm.projectOrTeamManager!''}-->
<#--        </td>-->
<#--    </tr>-->
    </tbody>
</table>
<h5>External Customer Information</h5>
<table class="table table-bordered">
    <tbody>
    <tr>
        <td class="bg-gray-light col-xs-3">Company Name or Institute Name</td>
        <td>
            ${approvalView.sopDisclosureRequestForm.companyNameOrInstituteName!''}
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">External Customer Name, Email, Job Title</td>
        <td>
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>No.</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Role</th>
                </tr>
                </thead>
                <tbody id="externalCustomerBody">
                <#list approvalView.sopDisclosureRequestForm.externalCustomers as c>
                    <tr>
                        <td>${c?index + 1}</td>
                        <td>${c.name}</td>
                        <td>${c.email}</td>
                        <td>${c.role!''}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Request Date<br/><small>(Date or Period to access ISO Management System)</small></td>
        <td>
            ${approvalView.sopDisclosureRequestForm.requestStartDate?has_content?then(approvalView.sopDisclosureRequestForm.requestStartDate?string('dd-MMM-yyyy'), '')} ~ ${approvalView.sopDisclosureRequestForm.requestStartDate?has_content?then(approvalView.sopDisclosureRequestForm.requestEndDate?string('dd-MMM-yyyy'), '')}
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Requested Documents</td>
        <td>
            <h6>[SOP/${formName}]</h6>
            <#list approvalView.sopDisclosureRequestForm.requestedDocumentSOPs as sop>
                <div>
                    <small><span class="label label-purple">SOP</span> ${sop.documentVersion.document.docId}/${sop.documentVersion.document.title}/${sop.documentVersion.version}</small>
                </div>
            </#list>
            <#if approvalView.sopDisclosureRequestForm.requestedDocumentRFs?has_content>
                <#list approvalView.sopDisclosureRequestForm.requestedDocumentRFs as rf>
                    <div>
                        <small><span class="label label-important label-info">${formName}</span> ${rf.documentVersion.document.docId}/${rf.documentVersion.document.title}/${rf.documentVersion.version}</small>
                    </div>
                </#list>
            </#if>
            <hr/>
            <h6>[Training Log(SOP)]</h6>
            <#if approvalView.sopDisclosureRequestForm.disclosureSOPTrainingLog?has_content>
                <#list approvalView.sopDisclosureRequestForm.disclosureSOPTrainingLog as sop>
                    <span class="label label-default">${sop.user.engName?default(sop.user.name)}</span>
                </#list>
            </#if>
            <h6>[Training Log(ISO)]</h6>
            <#if approvalView.sopDisclosureRequestForm.disclosureISOTrainingLog?has_content>
                <#list approvalView.sopDisclosureRequestForm.disclosureISOTrainingLog as iso>
                    <span class="label label-default">${iso.user.engName?default(iso.user.name)}</span>
                </#list>
            </#if>
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Type of Document Access</td>
        <td>
            ${approvalView.sopDisclosureRequestForm.documentAccess.label}
            <#if approvalView.sopDisclosureRequestForm.documentAccess == 'OTHER'>
                :<u>${approvalView.sopDisclosureRequestForm.documentAccessOther}</u>
            </#if>
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Purpose of Disclosure</td>
        <td>
            ${approvalView.sopDisclosureRequestForm.purposeOfDisclosure.label}
            <#if approvalView.sopDisclosureRequestForm.purposeOfDisclosure == 'OTHER'>
                :<u>${approvalView.sopDisclosureRequestForm.purposeOfDisclosureOther}</u>
            </#if>
        </td>
    </tr>

    </tbody>
</table>
