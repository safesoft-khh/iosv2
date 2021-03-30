<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#import "/common/pagination.ftl" as pagination />
<html>
<head>
    <title>ISO Management System > Admin > SOP/${formName} Access Log</title>
</head>
<body>

<div class="panel">
    <!--Data Table-->
    <!--===================================================-->
    <div class="panel-body">
        <@pagination.bind accessLog/>
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th class="text-center" style="width:70px;">No.</th>
                    <th class="text-center">Document Type</th>
                    <th>Document Id</th>
                    <th>Title</th>
                    <th class="text-center">Version</th>
                    <th class="text-center">Access Type</th>
                    <th class="text-center">Access Date</th>
                    <th class="text-center">User</th>
                    <#--                    <th class="text-center" style="width:120px">#actions</th>-->
                </tr>
                </thead>
                <tbody>
                <#if accessLog.content?has_content>
                    <#list accessLog.content as access>
                        <tr>
                            <td class="text-center">${(accessLog.totalElements - (accessLog.size * accessLog.number)) - access?index}
                            <td class="text-center"><span class="label label-${(access.documentVersion.document.type == 'SOP')?then('purple', 'mint')} label-table">${access.documentVersion.document.type.name()}</span></td>
                            <td>${access.documentVersion.document.docId}</td>
                            <td>${access.documentVersion.document.title}</td>
                            <td class="text-center">${access.documentVersion.version}</td>
                            <td class="text-center"><span class="label label-table label-${access.accessType.className}"> ${access.accessType.name()}</span></td>
                            <td class="text-center"><span class="text-muted">${access.createdDate?string('yyyy-MM-dd HH:mm:ss')}</span></td>
                            <td class="text-center">${access.createdBy?default('System')}</td>
                        </tr>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="8" class="text-center">Access Log가 없습니다.</td>
                    </tr>
                </#if>
                </tbody>
            </table>

        </div>
        <@pagination.default/>

        <#--        <nav style="float:left;">-->
        <#---->
        <#--        </nav>-->
        <#--        <@pagination.counter />-->
    </div>
    <!--===================================================-->
    <!--End Data Table-->

</div>
</body>
</html>