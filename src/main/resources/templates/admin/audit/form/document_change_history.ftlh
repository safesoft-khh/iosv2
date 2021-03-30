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
<#--            <th class="text-center">SOP ID </th>-->
            <th class="text-center">Document Type</th>
            <th class="text-center">Document Category</th>
            <th class="text-center">Document No</th>
            <th class="text-center">Doc Id</th>
            <th class="text-center">title</th>
            <th class="text-center">${formName} List</th>
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
<#--                    <td class="text-center ${new}">${audit.revisionEntity.rev!''}</td>-->
                    <td class="text-center ${new}">${audit.revisionEntity.revisionDate?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td class="text-center ${new}"><span class="label label-${revType}">${audit.revisionType}</span></td>
<#--                    <td class="text-center ${new}">${audit.entity.id}</td>-->
<#--                    <td class="text-center ${new}" style="${audit.contains('sop')?then('color:red;', '')}">${audit.entity.sop?has_content?then(audit.entity.sop.id, 'None')}</td>-->
                    <td class="text-center ${new}" style="${audit.contains('type')?then('color:red;', '')}">
                        <#if audit.entity.type=='SOP'>
                            <span class="label label-dark">${audit.entity.type}</span>
                        <#else>
                            <span class="label label-purple">${audit.entity.type}</span>
                        </#if>
                    </td>
                    <td class="text-center ${new}" style="${audit.contains('category')?then('color:red;', '')}">${audit.entity.category?has_content?then(audit.entity.category.shortName, 'N/A')}</td>
                    <td class="text-center ${new}" style="${audit.contains('documentNo')?then('color:red;', '')}">${audit.entity.documentNo!'N/A'}</td>
                    <td class="text-center ${new}" style="${audit.contains('docId')?then('color:red;', '')}">${audit.entity.docId!'N/A'}</td>
                    <td class="text-center ${new}" style="${audit.contains('title')?then('color:red;', '')}">${audit.entity.title!'N/A'}</td>
                    <td class="text-center ${new}">
                        <#if audit.entity.rfList?has_content>
                            <span class="badge badge-${audit.contains('rfList')?then('danger', 'gray')} add-popover"
                                  data-toggle="popover" data-container="body" data-placement="left"
                                  data-trigger="hover" data-html="true"
                                  data-content="<div class='pad-top'><#list audit.entity.rfList as rf><span class='label label-purple'>${rf.docId} :: ${rf.title}</span><br /></#list></div>">
                                <i class="fa fa-file-o"></i>&nbsp;(${audit.entity.rfList?size})
                            </span>
                        <#else>
                            N/A
                        </#if>
                    </td>
                </tr>
            </#list>
        <#else>
            <tr>
                <td colspan="11" class="text-center">Change History가 없습니다.</td>
            </tr>
        </#if>
        </tbody>
    </table>
</div>
<@pagination.default />