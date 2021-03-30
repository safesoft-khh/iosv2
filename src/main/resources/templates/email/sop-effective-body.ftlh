<#setting locale="en_US">
<#if effectiveSOPs?has_content>
    <h5>Effective SOP Information</h5>
    <table class="table table-bordered">
        <thead>
        <tr class="bg-gray-light">
            <th>Document ID</th>
            <th>Version</th>
            <th>Title</th>
            <th>Effective Date</th>
            <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <#list effectiveSOPs as sop>
            <tr>
                <td>${sop.document.docId}</td>
                <td>${sop.version}</td>
                <td>${sop.document.title}</td>
                <td>${sop.effectiveDate?string('dd-MMM-yyyy')}</td>
                <td>Effective</td>
            </tr>
        </#list>
        </tbody>
    </table>
</#if>
<#if effectiveRFs?has_content>
    <h5>Effective Form Information</h5>
    <table class="table table-bordered">
        <thead>
        <tr>
            <th>Document ID</th>
            <th>Version</th>
            <th>Title</th>
            <th>Effective Date</th>
            <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <#list effectiveRFs as rf>
            <tr>
                <td>${rf.document.docId}</td>
                <td>${rf.version}</td>
                <td>${rf.document.title}</td>
                <td>${rf.effectiveDate?string('dd-MMM-yyyy')}</td>
                <td>Effective</td>
            </tr>
        </#list>
        </tbody>
    </table>
</#if>
<#if retirementSOPs?has_content>
    <h5>Retirement SOP Information</h5>
    <table class="table table-bordered">
        <thead>
        <tr>
            <th>Document ID</th>
            <th>Version</th>
            <th>Title</th>
            <th>Retirement Date</th>
            <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <#list retirementSOPs as sop>
            <tr>
                <td>${sop.document.docId}</td>
                <td>${sop.version}</td>
                <td>${sop.document.title}</td>
                <td>${sop.retirementDate?string('dd-MMM-yyyy')}</td>
                <td>Retired</td>
            </tr>
        </#list>
        </tbody>
    </table>
</#if>
<#if retirementRFs?has_content>
    <h5>Retirement RF Information</h5>
    <table class="table table-bordered">
        <thead>
        <tr>
            <th>Document ID</th>
            <th>Version</th>
            <th>Title</th>
            <th>Retirement Date</th>
            <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <#list retirementRFs as rf>
            <tr>
                <td>${rf.document.docId}</td>
                <td>${rf.version}</td>
                <td>${rf.document.title}</td>
                <td>${rf.retirementDate?string('dd-MMM-yyyy')}</td>
                <td>Retired</td>
            </tr>
        </#list>
        </tbody>
    </table>
</#if>