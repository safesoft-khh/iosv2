<#import "/common/pagination.ftl" as pagination />
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#setting locale="en_US">
<html>
<head>
    <title>ISO Management System > Training > Employee Training</title>
    <script>
        $(document).ready(function () {
            $("#searchBtn").click(function () {
                $("#f").attr("method", "get").submit();
            });
        });
    </script>
</head>
<body>


<div class="panel">
    <div class="panel-heading">
        <h3 class="panel-title">SOP Training Log</h3>
    </div>
    <!--Data Table-->
    <!--===================================================-->
    <@pagination.bind trainingLog/>
    <div class="panel-body">
        <div class="pad-all">
            <form name="f" id="f">
                <#if userList?has_content>
                    <#assign pUserId = RequestParameters['userId']?has_content?then(RequestParameters['userId'], '0')?number/>
                    <div class="box-inline mar-btm">
                        User :
                        <div class="select">
                        <select name="userId" id="userList" class="form-control">
                            <option value=""${(pUserId == 0)?then('', ' selected')}>Select User</option>
                            <#list userList as user>
                                <option value="${user.id}"${(pUserId != 0)?then((pUserId == user.id)?then(' selected', ''), '')}>${user.name!''}</option>
                            </#list>
                        </select>
                        </div>
                    </div>
                </#if>
                <div class="box-inline mar-btm">
                    <button class="btn btn-default" style="margin-top:-5px !important;" type="button" id="searchBtn"><i
                                class="fa fa-search"></i></button>
                </div>
            </form>
        </div>
        <hr/>
        <#if logUser?has_content>
        <div class="tab-content">
            <div class="tab-pane fade active in">
                <#--                <#if isoTrainingLog.content?has_content>-->
                <#--                    <div class="text-right">-->
                <#--                        <form method="post" action="/training/trainingLog/publish">-->
                <#--                            <button class="btn btn-primary" id="exportBtn" type="button"><i class="fa fa-file"></i>-->
                <#--                                Export-->
                <#--                            </button>-->
                <#--                        </form>-->
                <#--                    </div>-->
                <#--                    <hr/>-->
                <#--                </#if>-->
                <h4 class="text-center">${logUser.engName?default(logUser.name)}</h4>
                <hr/>
                <table class="table table-bordered">
                    <tbody>
                    <tr>
                        <th class="col-xs-2 bg-gray-light">Affiliation:</th>
                        <td class="col-xs-4 text-primary">${siteCompanyTitle!''}</td>
                        <th class="col-xs-2 bg-gray-light">Department:</th>
                        <td class="col-xs-4 text-primary">${logUser.deptName!'N/A'}</td>
                    </tr>
                    <tr>
                        <th class="bg-gray-light">Date of Birth:</th>
                        <td class="text-primary">
                            ${logUser.birthDate?has_content?then(logUser.birthDate?string('dd-MMM-yyyy')?upper_case, 'N/A')}
                        </td>
                        <th class="bg-gray-light">Name:</th>
                        <td class="text-primary">${logUser.name!'N/A'}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="pad-btm">
                <p class="pad-no mar-no text-muted"><i class="fa fa-genderless"></i> This training log should be
                    completed by the employee; training hour can be included with specific mandatory training following
                    the regulation.</p>
                <p class="pad-no mar-no text-muted"><i class="fa fa-genderless"></i> Training course: MFDS required
                    training, Regulation, Guideline, On the Job Training, Various Type of Visit, External training,
                    On-line training etc.</p>
            </div>
            </#if>
            <div class="table-responsive">
                <table class="table table-bordered table-hover">
                    <thead>
                    <tr class="bg-gray-light">
                        <th class="text-center" style="width:70px;">No.</th>
                        <th class="text-center">Completion Date<br/><span
                                    class="text-muted text-sm"><em>(DD-MMM-YYYY)</em></span></th>
                        <th>Description of Training Course</th>
                        <th class="text-center">Training<br/>Hr.</th>
                        <th class="text-center">Organization<br/><span class="text-muted text-sm ital"><em>(Academy, Self-training, etc.)</em></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <#if trainingLog.content?has_content>
                        <#list trainingLog.content as t>
                            <tr>
                                <td class="text-center">${(trainingLog.totalElements - (trainingLog.size * trainingLog.number)) - t?index}</td>
                                <td class="text-center"><#if t.completeDate?has_content>${t.completeDate?string('dd-MMM-yyyy')}</#if></td>
                                <td>${t.trainingCourse}</td>
                                <td class="text-center">${t.hour}</td>
                                <td class="text-center">${t.organization}</td>
                            </tr>
                        </#list>
                    <#elseif logUser?has_content == false && trainingLog.content?has_content == false>
                        <tr>
                            <td colspan="5" class="text-center">User를 선택해 주세요.</td>
                        </tr>
                    <#else>
                        <tr>
                            <td colspan="5" class="text-center">Training Log가 없습니다.</td>
                        </tr>
                    </#if>
                    </tbody>
                </table>
            </div>
        </div>
        <@pagination.default/>
    </div>
    <!--===================================================-->
    <!--End Data Table-->

</div>
</body>
</html>
