<#import "/common/pagination.ftl" as pagination />
<#setting locale="en_US">

<@pagination.bind auditList/>
<div class="table-responsive">
    <table class="table table-hover table-bordered">
        <thead>
        <tr class="bg-gray-light">
<#--            <th class="text-center" style="width:80px">Rev No.</th>-->
            <th class="text-center">Revision Date</th>
            <th class="text-center">Revision Type</th>
<#--            <th class="text-center">ID</th>-->
            <th class="text-center">Report Type</th>
            <th class="text-center">Approval Status</th>
            <th class="text-center">Approval Lines</th>
            <th class="text-center">User Id</th>
            <th class="text-center">Deleted</th>
        </tr>
        </thead>
        <tbody>
        <#if auditList.content?has_content>
            <#list auditList.content as audit>
                <tr>
                    <#assign new = ''>
                    <#if audit.revisionType == 'ADD'>
                        <#assign revType = 'info' />
                        <#assign new = 'text-info text-bold bg-trans-dark'>
                    <#elseif audit.revisionType == 'MOD'>
                        <#assign revType = 'warning' />
                    <#elseif audit.revisionType == 'DEL'>
                        <#assign revType = 'danger' />
                    </#if>
<#--                <td class="text-center ${new}">${audit.revisionEntity.rev!''}</td>-->
                <td class="text-center ${new}">${audit.revisionEntity.revisionDate?string('yyyy-MM-dd HH:mm:ss')}</td>
                <td class="text-center ${new}"><span class="label label-${revType}">${audit.revisionType}</span></td>
<#--                <td class="text-center ${new}">${audit.entity.id}</td>-->
                <td class="text-center ${new}" style="${audit.contains('type')?then('color:red;', '')}">${audit.entity.type.label!'N/A'}</td>
                <td class="text-center ${new}" style="${audit.contains('status')?then('color:red;', '')}">${audit.entity.status!'N/A'}</td>
                <td class="text-center ${new}">
                    <span class="label ${new?has_content?then('label-info', audit.contains('approvalLines')?then('label-danger', 'bg-gray-dark'))} add-popover"
                          data-toggle="popover" data-container="body" data-placement="top"
                          data-trigger="hover" data-html="true"
                          data-content="<div><#list audit.entity.approvalLines as approvalLine><p><i class='fa fa-check-square'></i>&nbsp;${approvalLine.lineType.label!''}&nbsp;${approvalLine.displayName}</p></#list></div>"
                          style="max-width: 160px; cursor: pointer;">
                        <i class="fa fa-search"></i>
                    </span>
                </td>
                <td class="text-center ${new}" style="${audit.contains('username')?then('color:red;', '')}">${audit.entity.username!'N/A'}</td>
                <td class="text-center ${new}" style="${audit.contains('deleted')?then('color:red;', '')}">${audit.entity.deleted?then('Y','N')}</td>

            </#list>
        <#else>
            <tr>
                <td colspan="9" class="text-center">Change History가 없습니다.</td>
            </tr>
        </#if>
        </tbody>
    </table>
</div>
<@pagination.default />