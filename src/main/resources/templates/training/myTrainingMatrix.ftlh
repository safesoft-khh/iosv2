<#--<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />-->
<#import "/common/pagination.ftl" as pagination />
<#--<@security.authentication property="principal" var="user"/>-->
<#--<#setting locale="en_US">-->
<html>
<head>
    <title>Training > My Training Matrix</title>
</head>
<body>


<div class="panel">
    <div class="panel-heading">
        <h3 class="panel-title">My Training Matrix</h3>
    </div>

    <!--Data Table-->
    <!--===================================================-->
    <@pagination.bind trainingMatrix/>
    <#assign jdSize = userJobDescriptions?has_content?then(userJobDescriptions?size, 0)/>
    <div class="panel-body">
        <#if jdSize == 0>
        <div class="text-danger">
            <i class="fa fa-exclamation-circle"> ROLE이 지정 되어 있지 않습니다. 관리자에게 문의해 주세요.</i>
        </div>
        </#if>
        <div class="table-responsive">
            <table class="table table-hover table-striped">
                <thead>
                <tr>
                    <th class="text-center" colspan="5">SOP List</th>
                    <th<#if jdSize gt 1> colspan="${jdSize}"</#if> class="text-center">Role</th>
                </tr>
                <tr>
                    <th class="text-center" style="width:60px;">No.</th>
                    <th class="col-xs-3">SOP Document ID</th>
                    <th>Title</th>
                    <th>Version</th>
                    <th>Effective Date</th>
<#--                    <th class="col-xs-2 text-center">QMO</th>-->
<#--                    <th class="col-xs-2 text-center">QAM</th>-->
                    <#if userJobDescriptions?has_content>
                    <#list userJobDescriptions as jd>
                        <th class="text-center">
<#--                            <span class="text-sm">${jd.jobDescription.title}</span><br/>-->
                            ${jd.jobDescription.shortName}
                        </th>
                    </#list>
                    <#else>
                        <th class="text-center">
                            All
                        </th>
                    </#if>
                </tr>
                </thead>
                <tbody>
                <#if trainingMatrix.content?has_content>
                    <#list trainingMatrix.content as t>
                    <tr>
                        <td class="text-center">${(trainingMatrix.totalElements - (trainingMatrix.size * trainingMatrix.number)) - t?index}</td>
                        <td>${t.document.docId}</td>
                        <td>${t.document.title}</td>
                        <td>${t.documentVersion.version}</td>
                        <td>${t.documentVersion.effectiveDate?string('yyyy-MM-dd')}</td>
                        <#if userJobDescriptions?has_content>
                        <#list userJobDescriptions as jd>
                        <td class="text-center">
                            <#list t.documentVersion.trainingMatrixList as m>
                                <#if m.trainingAll == true>
                                    <i class="fa fa-check text-success"></i>
                                <#elseif m.jobDescription.id = jd.jobDescription.id>
                                    <i class="fa fa-check text-success"></i>
                                </#if>
                            </#list>
                        </td>
                        </#list>
                        <#else>
                            <td class="text-center">
                                <i class="fa fa-check text-success"></i>
                            </td>
                        </#if>
                    </tr>
                </#list>
                <#else>
                    <tr>
                        <td class="text-center" colspan="${jdSize + 6}"><i class="fa fa-exclamation-circle text-info"></i> SOP 정보가 존재하지 않습니다.</td>
                    </tr>
                </#if>
                </tbody>
            </table>
        </div>
<#--        <@pagination.default/>-->
    </div>
    <!--===================================================-->
    <!--End Data Table-->

</div>

</body>
</html>