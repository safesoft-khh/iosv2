<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#import "/common/pagination.ftl" as pagination />
<@security.authentication property="principal" var="user"/>
<#setting locale="en_US">
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>ISO Management System > SOP > Training Log</title>
</head>
<body>
<@pagination.bind isoTrainingLog/>
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
            <#if isoTrainingLog.content?has_content>
            <div class="text-right">
                <form method="post" action="/training/iso/trainingLog/export">
                    <button class="btn btn-primary" type="submit"><i class="wi-fire"></i> Export</button>
                </form>
            </div>
            <hr/>
            </#if>
            <h4 class="text-center">${user.engName?default(user.name)}</h4>
            <hr/>
            <table class="table table-bordered">
                <tbody>
                <tr>
                    <th class="col-xs-2 bg-gray-light">Affiliation:</th>
                    <td class="col-xs-4 text-primary">${siteCompanyTitle!''}</td>
                    <th class="col-xs-2 bg-gray-light">Department:</th>
                    <td class="col-xs-4 text-primary">${user.deptName!'N/A'}</td>
                </tr>
                <tr>
                    <th class="bg-gray-light">Date of Birth:</th>
                    <td class="text-primary">
                        ${user.birthDate?has_content?then(user.birthDate?string('dd-MMM-yyyy')?upper_case, 'N/A')}
                    </td>
                    <th class="bg-gray-light">Name:</th>
                    <td class="text-primary">${user.name!'N/A'}</td>
                </tr>
                </tbody>
            </table>
        </div>

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
                <#if isoTrainingLog.content?has_content>
                    <#list isoTrainingLog.content as t>
                        <tr>
                            <td class="text-center">${(isoTrainingLog.totalElements - (isoTrainingLog.size * isoTrainingLog.number)) - t?index}</td>
                            <td class="text-center"><#if t.completeDate?has_content>${t.completeDate?string('dd-MMM-yyyy')}</#if></td>
                            <td>${t.trainingCourse}</td>
                            <td class="text-center">${t.hour}</td>
                            <td class="text-center">${t.organization}</td>
                        </tr>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="5" class="text-center">
                            Training Log 내용이 없습니다.
                        </td>
                    </tr>
<#--                    <tr>-->
<#--                        <td colspan="5">-->
<#--                            <div class="text-info pad-top pad-btm"><i class="fa fa-exclamation-circle"></i> 최초 1회만 Training Log 를 가져오기 할 수 있습니다.</div>-->
<#--                            <form name="f" id="importForm" method="post" enctype="multipart/form-data" class="form-inline">-->
<#--                                <label class="control-label"><i class="pli-file-word"></i> Employee Training Log(SOP) <input type="file" name="file" id="logFile" accept=".docx" class="form-control"/></label>-->
<#--                                <button class="btn btn-info" type="submit"><i class="fa fa-upload"></i> Upload</button>-->
<#--                            </form>-->
<#--                            <script>-->
<#--                                $("#importForm").submit(function(e) {-->
<#--                                   if(!$("#logFile").val()) {-->
<#--                                       e.preventDefault();-->
<#--                                       alert('Training Log(SOP) MS Word 파일을 선택해 주세요.');-->
<#--                                   }-->
<#--                                });-->
<#--                            </script>-->
<#--                        </td>-->
<#--                    </tr>-->
                </#if>
                </tbody>
            </table>
        </div>
        <@pagination.default/>
    </div>
</div>
<#--<div class="panel">-->
<#--    <div class="panel-heading">-->
<#--        <h3 class="panel-title">Training Log <small class="text-danger">트레이닝 전체 이력!!(테스트 실패 포함?!)</small></h3>-->
<#--    </div>-->
<#--    -->
<#--    <!--Data Table&ndash;&gt;-->
<#--    <!--===================================================&ndash;&gt;-->
<#--    <div class="panel-body">-->
<#--        <div class="pad-btm form-inline">-->
<#--            <div class="row">-->
<#--                <div class="col-sm-6 table-toolbar-left">-->
<#--                    &lt;#&ndash;                    <a class="btn btn-purple" href="/notice/new"><i class="pli-add"></i> Add</a>&ndash;&gt;-->
<#--                    &lt;#&ndash;                    <button class="btn btn-default"><i class="pli-printer"></i></button>&ndash;&gt;-->
<#--                    &lt;#&ndash;                    <div class="btn-group">&ndash;&gt;-->
<#--                    &lt;#&ndash;                        <button class="btn btn-default"><i class="pli-exclamation"></i></button>&ndash;&gt;-->
<#--                    &lt;#&ndash;                        <button class="btn btn-default"><i class="pli-recycling"></i></button>&ndash;&gt;-->
<#--                    &lt;#&ndash;                    </div>&ndash;&gt;-->
<#--                </div>-->
<#--                <div class="col-sm-6 table-toolbar-right">-->
<#--&lt;#&ndash;                    <div class="form-group">&ndash;&gt;-->
<#--&lt;#&ndash;                        <input id="demo-input-search2" type="text" placeholder="Document ID" class="form-control" autocomplete="off">&ndash;&gt;-->
<#--&lt;#&ndash;                    </div>&ndash;&gt;-->
<#--&lt;#&ndash;                    <div class="btn-group">&ndash;&gt;-->
<#--                        <a class="btn btn-primary" href="trainingLog/download"><i class="fa fa-file-pdf-o"></i> Training Log</a>-->
<#--&lt;#&ndash;                    </div>&ndash;&gt;-->
<#--                </div>-->
<#--            </div>-->
<#--        </div>-->
<#--        <div class="table-responsive">-->
<#--            <table class="table table-striped table-bordered">-->
<#--                <thead>-->
<#--                <tr>-->
<#--                    <th class="text-center" style="width:70px;">No.</th>-->
<#--                    <th class="text-center">Completion Date<br/><span class="text-muted text-sm"><em>(DD-MMM-YYYY)</em></span> </th>-->
<#--                    <th>Description of Training Course</th>-->
<#--                    <th class="text-center">Training<br/>Hr.</th>-->
<#--                    <th>Organization<br/><span class="text-muted text-sm ital"><em>(Academy, Self-training, etc.)</em></span> </th>-->
<#--                </tr>-->
<#--                </thead>-->
<#--                <tbody>-->
<#--                <#if trainingLog.content?has_content>-->
<#--                <#list trainingLog.content as t>-->
<#--                    <tr>-->
<#--                        <td class="text-center">${(trainingLog.totalElements - (trainingLog.size * trainingLog.number)) - t?index}</td>-->
<#--                        <td class="text-center">${t.completeDate?string('dd-MMM-yyyy')}</td>-->
<#--                        <td>${t.trainingCourse}</td>-->
<#--                        <td class="text-center">${t.hour}</td>-->
<#--                        <td>${t.organization}</td>-->
<#--                    </tr>-->
<#--                </#list>-->
<#--                <#else>-->
<#--                    <tr>-->
<#--                        <td colspan="5">-->
<#--                            <form name="f">-->
<#--                                <label>Training 이력 가져오기 <input type="file" accept=".xls,.xlsx"/></label>-->
<#--                            </form>-->
<#--                        </td>-->
<#--                    </tr>-->
<#--                </#if>-->
<#--                <tr>-->
<#--                    <td colspan="5">-->
<#--                        <form name="f">-->
<#--                            <label>Training 이력 가져오기 <input type="file" accept=".xls,.xlsx"/></label>-->
<#--                        </form>-->
<#--                    </td>-->
<#--                </tr>-->
<#--                </tbody>-->
<#--            </table>-->
<#--        </div>-->
<#--        <@pagination.default/>-->
<#--    </div>-->
<#--    <!--===================================================&ndash;&gt;-->
<#--    <!--End Data Table&ndash;&gt;-->

<#--</div>-->
</body>
</html>
