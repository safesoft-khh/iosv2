<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#import "/common/pagination.ftl" as pagination />
<@security.authentication property="principal" var="user"/>
<#setting locale="en_US">
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>ISO Management System > SOP > Training Log(Import)</title>

</head>
<body>
<div class="tab-base">

    <!--Nav Tabs-->
    <#--    <ul class="nav nav-tabs">-->
    <#--        <#list user.jobDescriptions as jd>-->
    <#--        <li<#if jd?is_first> class="active"</#if>>-->
    <#--            <a data-toggle="tab" href="#jd-tab-${jd?index}">${jd.jobDescription.shortName}</a>-->
    <#--        </li>-->
    <#--        </#list>-->
    <#--    </ul>-->

    <!--Tabs Content-->
    <div class="tab-content">

        <div class="tab-pane fade active in">
<#--            <div class="text-right">-->
<#--                <a class="btn btn-primary" href="trainingLog/download"><i class="fa fa-file-pdf-o"></i> Training Log</a>-->
<#--            </div>-->
<#--            <hr/>-->
            <h4 class="text-center">${user.engName?default(user.name)}</h4>
            <hr/>
            <table class="table table-bordered">
                <tbody>
                <tr>
                    <th class="col-xs-2 bg-gray-light">Role:</th>
                    <td class="col-xs-4 text-primary">${user.commaJobDescription}</td>
                    <th class="col-xs-2 bg-gray-light">Start Date in ${siteCode}:</th>
                    <td class="col-xs-4 text-primary">${user.indate?string('dd-MMM-yyyy')}</td>
                </tr>
                <tr>
                    <th class="bg-gray-light">Department/Team:</th>
                    <td class="text-primary">
                        <#if user.orgDepart?has_content>${user.orgDepart}</#if><#if user.orgTeam?has_content><#if user.orgDepart?has_content>/</#if>${user.orgTeam}</#if>
                    </td>
                    <th class="bg-gray-light">Employee No.:</th>
                    <td class="text-primary">${user.comNum}</td>
                </tr>
                </tbody>
            </table>
        </div>

        <#--        <#list user.jobDescriptions as jd>-->
        <#--        <div id="jd-tab-${jd?index}" class="tab-pane<#if jd?is_first> fade active in</#if>">-->
        <#--            <div class="text-right">-->
        <#--                <a class="btn btn-primary" href="trainingLog/download"><i class="fa fa-file-pdf-o"></i> Training Log(${jd.jobDescription.shortName})</a>-->
        <#--            </div>-->
        <#--            <hr/>-->
        <#--            <h4 class="text-center">${user.engName?default(user.korName)}</h4>-->
        <#--            <hr/>-->
        <#--            <table class="table table-bordered">-->
        <#--                <tbody>-->
        <#--                <tr>-->
        <#--                    <th class="col-xs-2 bg-gray-light">Job Title:</th>-->
        <#--                    <td class="col-xs-4 text-primary">${jd.jobDescription.title}(${jd.jobDescription.shortName})</td>-->
        <#--                    <th class="col-xs-2 bg-gray-light">Start Date in Dt&SanoMedics:</th>-->
        <#--                    <td class="col-xs-4 text-primary">${user.inDate?string('dd-MMM-yyyy')}</td>-->
        <#--                </tr>-->
        <#--                <tr>-->
        <#--                    <th class="bg-gray-light">Department/Team:</th>-->
        <#--                    <td class="text-primary">-->
        <#--                        <#if user.deptName?has_content>${user.deptName}/</#if><#if user.teamName?has_content>${user.teamName}</#if>-->
        <#--                    </td>-->
        <#--                    <th class="bg-gray-light">Employee No.:</th>-->
        <#--                    <td class="text-primary">${user.empNo}</td>-->
        <#--                </tr>-->
        <#--                </tbody>-->
        <#--            </table>-->
        <#--        </div>-->
        <#--        </#list>-->
        <div class="pad-btm">
            <p class="pad-no mar-no text-muted"><i class="fa fa-genderless"></i> This training log should be completed by the employee; training hour can be included with specific mandatory training following the regulation.</p>
            <p class="pad-no mar-no text-muted"><i class="fa fa-genderless"></i> Training course: MFDS required training, Regulation, Guideline, On the Job Training, Various Type of Visit, External training, On-line training etc.</p>
        </div>
        <div class="table-responsive">
            <table class="table table-bordered table-hover">
                <thead>
                <tr class="bg-gray-light">
                    <th class="text-center" style="width:70px;">No.</th>
                    <th class="text-center">Completion Date<br/><span class="text-muted text-sm"><em>(DD-MMM-YYYY)</em></span> </th>
                    <th>Description of Training Course</th>
                    <th class="text-center">Training<br/>Hr.</th>
                    <th class="text-center">Organization<br/><span class="text-muted text-sm ital"><em>(Academy, Self-training, etc.)</em></span> </th>
                </tr>
                </thead>
                <tbody>
                <#if trainingLogs?has_content>
                    <#list trainingLogs as t>
                        <tr>
                            <td class="text-center">${t?index + 1}</td>
                            <td class="text-center">
                                <#if t.completeDate?has_content>
                                    ${t.completeDate?string('dd-MMM-yyyy')}
                                <#else>
                                    <small class="text-danger"><i class="fa fa-exclamation"></i> 날짜형식오류</small>
                            </#if></td>
                            <td>
                                <#if t.importTrainingCourse?has_content>
                                    <span<#if t.matched == false> class="text-warning"</#if>>${t.importTrainingCourse}</span>
                                    <#if t.matched>
                                        <small class="text-danger"><i class="fa fa-exclamation"></i> 시스템에 등록되지 않은 SOP 정보 입니다.(관리자에게 문의해 주세요.)</small>
                                    <#else>
                                    <small class="text-danger"><i class="fa fa-exclamation"></i> Training Course 형식을 수정해 주세요.</small>
                                    </#if>
                                <#else>
                                    ${t.documentVersion.document.docId!''} v${t.documentVersion.version} ${t.documentVersion.document.title!''}
                                </#if>
                            </td>
                            <td class="text-center">${t.hour}</td>
                            <td class="text-center">${t.organization}</td>
                        </tr>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="5">
                            Training Log 정보가 없습니다.
                        </td>
                    </tr>
                </#if>
                </tbody>
            </table>
            <div class="text-right">
                <form name="f" method="post">
                    <input type="hidden" name="_method" value="put"/>

                    <a class="btn btn-warning" href="/training/trainingLog"><i class="fa fa-chevron-left"></i> Back</a>
                    <#if hasError == false>
                    <button class="btn btn-primary" type="submit"><i class="fa fa-database"></i> Training Log(저장)</button>
                    </#if>
                </form>
            </div>
        </div>
    </div>
</div>

<#--</div>-->
</body>
</html>
