<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#import "/common/pagination.ftl" as pagination />
<html>
<head>
    <title>Admin > ISO Access Log</title>

</head>
<body>



<div class="panel">
    <!--Data Table-->
    <!--===================================================-->
    <div class="panel-body">
        <@pagination.bind isoAccessLog/>
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th class="text-center" style="width:70px;">No.</th>
                    <th class="text-center">ISO Type</th>
                    <th>ISO Info <span class="text-muted text-sm"><em>(Title / Cert No. / Cert Title)</em></th>
                    <th class="text-center">Access Type</th>
                    <th class="text-center">Access Date</th>
                    <th class="text-center">User</th>
                    <#--                    <th class="text-center" style="width:120px">#actions</th>-->
                </tr>
                </thead>
                <tbody>
                <#if isoAccessLog.content?has_content>
                    <#list isoAccessLog.content as access>
                        <tr>
                            <td class="text-center">${(isoAccessLog.totalElements - (isoAccessLog.size * isoAccessLog.number)) - access?index}
                            <td class="text-center"><span class="label label-${access.isoType.className} label-table">${access.isoType.label}</span></td>
                            <#if access.isoType == "ISO_14155" && access.iso?has_content>
                                <td>${access.iso.title}</td>
                            <#elseif access.isoType == "ISO_CERT_STATUS" && access.isoCertification?has_content>
                                <td>${access.isoCertification.title}</td>
                            <#elseif access.isoType == "ISO_14155_CERT" && access.isoTrainingCertification?has_content>
                                <td>${access.isoTrainingCertification.id}</td>
                            </#if>
                            <td class="text-center"><span class="label label-table label-${access.accessType.className}"> ${access.accessType.name()}</span></td>
                            <td class="text-center"><span class="text-muted">${access.createdDate?string('yyyy-MM-dd HH:mm:ss')}</span></td>
                            <td class="text-center">${access.createdBy?default('System')}</td>
                        </tr>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="8" class="text-center">ISO Access Log가 없습니다.</td>
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