<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#import "/common/pagination.ftl" as pagination />
<html>
<head>
    <title> ISO Management System > ISO > ISO-14155</title>
    <#--    <script src="/static/plugins/jstree/jstree.min.js"></script>-->
    <#--    <link href="/static/plugins/jstree/themes/default/style.min.css" rel="stylesheet">-->

    <!--Bootbox Modals [ OPTIONAL ]-->
    <script src="/static/plugins/bootbox/bootbox.min.js"></script>
    <!-- Mask -->
    <script src="/static/js/jquery.mask.min.js"></script>

    <style>
        .modal-dialog.modal-fullsize {
            width: 80%;
            height: 550px;
            margin: 3px;
            padding: 0;
        }

        .modal-content.modal-fullsize {
            height: auto;
            min-height: 550px;
            border-radius: 0;
        }
    </style>
    <script>

        function numberPad(n, width) {
            n = n + '';
            return n.length >= width ? n : new Array(width - n.length + 1).join('0') + n;
        }

        //추가 일수 입력란 변경 시 이벤트.
        function dateChange(start, end, date) {
            let startDate = new Date(start);
            let endDate = new Date(end);
            let expandDate = $("#addDayField").val();

            if(expandDate == "") {
                $("#originDate").css("text-decoration", "");
                $("#newDate").removeClass("class");
                $("#newDate").removeClass("label label-default");
                $("#newDate").html("");
            } else {
                $("#originDate").css("text-decoration", "line-through");
                let res;
                let time = $("#addDayField").val();
                endDate.setDate(endDate.getDate() + parseInt(time));

                if(!$("#newDate").hasClass()) {
                    $("#newDate").addClass("class");
                    $("#newDate").addClass("label label-default");
                }

                if(startDate.getTime() == endDate.getTime()) {
                    res = "-> " + start;
                } else {
                    res = "-> " + start + " ~ " + numberPad(endDate.getFullYear(), 4) + '-' +
                        numberPad((endDate.getMonth()+1), 2) + '-' + numberPad(endDate.getDate(), 2);
                }

                $("#newDate").html(res);
            }

        }

        $(document).ready(function () {

            $(document).on("click", "#expandBtn", function(){
                let isoId = $(this).data("id");
                let start = $(this).data("start");
                let end = $(this).data("end");
                let name = $(this).data("name");
                let date = $(this).data("date");

                bootbox.dialog({
                    title: 'Training Period Expand',
                    message: "<div>"
                        + "<p><span class='label label-primary'>[" + name + "]</span>의 Training 기한을 연장합니다.</p>"
                        + "<table class='table table-striped'>"
                        + "<tr>"
                        + "<th style='width:250px'>Training Date</th>"
                        + "<th>Action#</th>"
                        + "</tr>"
                        + "<tr>"
                        + "<td><span id='originDate' class='label label-default'>" + date + "</span><br><br><span id='newDate'></span></td>"
                        + "<td><input type='number' min='1' max='30' id='addDayField' class='form-control' placeholder='연장 일수 입력' onkeyup='dateChange(\"" + start + "\",\"" + end + "\",\"" + date + "\")' /></td>"
                        + "</tr>"
                        + "</table>"
                        + "</div>"
                        + "<script>$('#addDayField').mask('00');<\/script>",
                    size: 'middle',
                    buttons: {
                        cancel: {
                            label: "Cancel",
                            className: 'btn-danger'
                        },
                        ok: {
                            label: "OK",
                            className: 'btn-info',
                            callback: function(e){
                                let time = $("#addDayField").val();

                                if(time == "") {
                                    alert("연장 일수를 입력해주세요.");
                                    return false;
                                }

                                $("#expandIsoId").val(isoId);
                                $("#addDays").val(time);
                                $("#expandForm").submit();
                            }
                        }
                    }

                })

            })



            $(document).on("click", "#activeBtn", function(){
                let isoId = $(this).data("id");
                let test = $(this).data('test');
                let testColor = test=='Y'?'success':'warning';

                let correct = $(this).data('correct');

                let attendee = $(this).data('attendee');
                let attendeeColor = attendee=='ALL'?'info':'purple';

                let date = $(this).data('date');

                //startDate 지점이 오늘날짜 기준 과거인 경우, 에러 문구 표시.
                let strStartDate = date.indexOf('~')!=-1?date.substr(0,date.indexOf('~')).trim():date;
                let today = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate());
                let startDate = strToDate(strStartDate);
                let error = startDate < today?"<br><span class='text-sm' style='color:red;'>*교육 일정에 대한 재 조정이 필요합니다.</span>":"";

                bootbox.dialog({
                    title: 'Training Activate',
                    message: "<div>"
                        + "<p>아래의 정보로 Training을 활성화 합니다. 계속 하시겠습니까?</p>"
                        + "<table class='table table-striped'>"
                        + "<tr>"
                        + "<th>시험 여부 : </th>"
                        + "<td><span class='label label-" + testColor + "'>" + test + "</span></td>"
                        + "</tr>"
                        + "<tr>"
                        + "<th>합격 문항 개수 : </th>"
                        + "<td><span class='label label-success'>" + correct + "</span></td>"
                        + "</tr>"
                        + "<tr>"
                        + "<th>교육 참석자 : </th>"
                        + "<td><span class='label label-" + attendeeColor + "'>" + attendee + "</span></td>"
                        + "</tr>"
                        + "<tr>"
                        + "<th>교육 일정 : </th>"
                        + "<td>"
                        + "<span class='label label-default'>" + date + "</span>" + error
                        + "</td>"
                        + "</tr>"
                        + "</table>"
                        + "</div>",
                    size: 'middle',
                    buttons: {
                        cancel: {
                            label: "Cancel",
                            className: 'btn-danger'
                        },
                        ok: {
                            label: "OK",
                            className: 'btn-info',
                            callback: function(){
                                $("#activeIsoId").val(isoId);
                                $("#activeForm").submit();
                            }
                        }
                    }
                });
            });
        });

        function strToDate(date_str)
        {
            var yyyyMMdd = String(date_str);
            var sYear = yyyyMMdd.substring(0,4);
            var sMonth = yyyyMMdd.substring(5,7);
            var sDate = yyyyMMdd.substring(8,10);

            //alert("sYear :"+sYear +"   sMonth :"+sMonth + "   sDate :"+sDate);
            return new Date(Number(sYear), Number(sMonth)-1, Number(sDate));
        }
    </script>
</head>
<body>

<div class="tab-base">
    <ul class="nav nav-tabs">
        <li>
            <a href="/iso-14155/board?categoryCode=${categoryCode}">Board</a>
        </li>
        <li class="active">
            <a href="/iso-14155/training?categoryCode=${categoryCode}">Training</a>
        </li>
    </ul>
    </ul>
    <div class="tab-content">
        <div class="pad-btm form-inline">
            <div class="row">
                <form name="f" id="f" class="form-inline">
                    <#if categoryList?has_content>
                    <#--                <#assign pDeptCode = RequestParameters['categoriList']?default('')/>-->
                        <div class="box-inline mar-btm">
                            <select name="categoryCode" id="categoryCode" class="form-control">
                                <option value=""}>ALL</option>
                                <#list categoryList as category>
                                    <#if categoryCode?has_content&&categoryCode == category.getShortName()>
                                        <option value="${category.getShortName()}" selected>${category.getShortName()}</option>
                                    <#else>
                                        <option value="${category.getShortName()}">${category.getShortName()}</option>
                                    </#if>
                                </#list>
                            </select>
                            <a class="col-sm-12 table-toolbar-left">
                                <@security.authorize access="hasAnyAuthority('ADMIN')">
                                    <a class="btn btn-primary" onclick="registfunction()"><i
                                                class="pli-pencil"></i> 등록</a>
                                </@security.authorize>

                                <script>
                                    function registfunction(){
                                        var code = document.getElementById("categoryCode").value;
                                        if(code != "")
                                            location.href="/iso-14155/training/new?categoryCode="+code+"";
                                        else
                                            alert("Category를 선택해 주세요");
                                    }
                                </script>

                            </a>
                        </div>

                        <script>
                            $("#categoryCode").change(function() {
                                document.getElementById("categoryCode").value;
                                //$("#categoryCode").val("");
                                $("#f").attr("method", "get").submit();
                            });
                        </script>
                    </#if>
                </form>
            </div>
        </div>
        <@pagination.bind isoList/>

        <#if categoryCode?has_content>
            <input type="hidden"  id="categoryCode", name = "categoryCode" value="${categoryCode}">
        </#if>
        <#--        <div class="table-responsive">-->
        <table class="table">
            <thead>
            <tr>
                <th class="text-center" style="width:70px;">No.</th>
                <th class="text-center" style="width:90px;">Type</th>
                <th class="text-center" style="width:90px;">Category</th>
                <th>Title</th>
                <@security.authorize access="hasAnyAuthority('ADMIN')">
                    <th class="text-center" style="width:90px;">시험</th>
                </@security.authorize>
                <th class="text-center" style="width:200px;">교육 일정</th>
                <th class="text-center" style="width:120px;">등록일</th>
                <th class="text-center" style="width:100px;">작성자</th>
                <@security.authorize access="hasAnyAuthority('ADMIN')">
                    <th class="text-center" style="width:140px">Activate</th>
                </@security.authorize>
            </tr>
            </thead>
            <tbody>
            <#if topISOList?has_content>
                <#list topISOList as iso>
                    <tr class="bg-gray-light">
                        <td class="text-center"><i class="fa fa-exclamation-circle text-warning"></i></td>
                        <td class="text-center"><span class="label label-${iso.training?then('info','mint')}">${iso.training?then('Training','Board')}</span></td>

<#--                        <#if iso.category?has_content>-->
<#--                            <td class="text-center"><span class="text-muted">${iso.getCategory().getShortName()}</span></td>-->
<#--                        <#else>-->
<#--                            <td class="text-center"><span class="text-muted"></span></td>-->
<#--                        </#if>-->

<#--                        <td>-->
<#--                            <a class="btn-link" href="/iso-14155/${iso.id}">-->
<#--                                ${iso.title}-->
<#--                            </a>-->
<#--                        </td>-->

                        <#if iso.category?has_content>
                            <td class="text-center"><span class="label label-pink">${iso.getCategory().getShortName()}</span></td>
                            <td><a class="btn-link" href="/iso-14155/${iso.id}?categoryCode=${iso.getCategory().getShortName()}">${iso.title}</a></td>
                        <#else>
                            <td class="text-center"><span class="label label-pink"></span></td>
                            <td><a class="btn-link" href="/iso-14155/${iso.id}">${iso.title}</a></td>
                        </#if>


                        <@security.authorize access="hasAnyAuthority('ADMIN')">
                            <td class="text-center">
                                <!--===================================================-->
                                <div class="btn-group btn-group-xs dropdown">
                                    <button class="btn btn-${(iso.quiz?has_content)?then('info', 'default')} btn-active-pink dropdown-toggle dropdown-toggle-icon" data-toggle="dropdown" type="button">
                                        Quiz <i class="dropdown-caret"></i>
                                    </button>
                                    <ul class="dropdown-menu">
                                        <li><a href="/iso-14155/${iso.id}/quiz"><i class="fa fa-pencil"></i> Edit</a></li>
                                        <li><a href="/iso-14155/${iso.id}/quiz/upload"><i class="fa fa-upload"></i> Template Upload</a></li>
                                        <#if iso.quiz?has_content>
                                            <li class="divider"></li>
                                            <li><a href="/iso-14155/${iso.id}/quiz/test"><i class="fa fa-check-square-o"></i> Test</a></li>
                                        </#if>
                                    </ul>
                                </div>
                                <!--===================================================-->
                            </td>
                        </@security.authorize>
                        <td class="text-center"><span class="text-info">${iso.trainingDate}</span></td>
                        <td class="text-center"><span class="text-muted">${iso.createdDate?string('yyyy-MM-dd')}</span></td>
                        <td class="text-center">${iso.createdBy?default('System')}</td>
                        <@security.authorize access="hasAnyAuthority('ADMIN')">
                            <td class="text-center">
                                <#if iso.training>
                                    <#if iso.active>
                                        <button id="expandBtn" class="btn btn-primary btn-xs" data-id="${iso.id}"
                                                data-start="${iso.startDate?string('yyyy-MM-dd')}" data-date="${iso.trainingDate}"
                                                data-end="${iso.endDate?string('yyyy-MM-dd')}" data-name="${iso.title}">
                                            Period Expand
                                        </button>
                                    <#else>
                                        <button id="activeBtn" data-date="${iso.trainingDate}" data-attendee="${iso.attendee}" data-correct="${iso.correctCount}"
                                                data-test="${iso.quiz?has_content?then('Y', 'N')}" data-id="${iso.id}" class="btn btn-purple btn-xs">Activate</button>
                                    </#if>
                                <#else>
                                    -
                                </#if>
                            </td>
                        </@security.authorize>
                    </tr>
                </#list>
            </#if>
            <#if isoList.content?has_content>
                <#list isoList.content as iso>
                    <tr>
                        <td class="text-center">${(isoList.totalElements - (isoList.size * isoList.number)) - iso?index}</td>
                        <td class="text-center"><span class="label label-${iso.training?then('info','mint')}">${iso.training?then('Training','Board')}</span></td>

<#--                        <#if iso.category?has_content>-->
<#--                            <td class="text-center"><span class="text-muted">${iso.getCategory().getShortName()}</span></td>-->
<#--                        <#else>-->
<#--                            <td class="text-center"><span class="text-muted"></span></td>-->
<#--                        </#if>-->

<#--                        <td>-->
<#--                            <a class="btn-link" href="/iso-14155/${iso.id}">${iso.title}</a>-->
<#--                        </td>-->

                        <#if iso.category?has_content>
                            <td class="text-center"><span class="label label-pink">${iso.getCategory().getShortName()}</span></td>
                            <td><a class="btn-link" href="/iso-14155/${iso.id}?categoryCode=${iso.getCategory().getShortName()}">${iso.title}</a></td>
                        <#else>
                            <td class="text-center"><span class="label label-pink"></span></td>
                            <td><a class="btn-link" href="/iso-14155/${iso.id}">${iso.title}</a></td>
                        </#if>

                        <@security.authorize access="hasAnyAuthority('ADMIN')">
                            <td class="text-center">
                                <#if iso.training>
                                    <!--===================================================-->
                                    <div class="btn-group btn-group-xs dropdown">
                                        <button class="btn btn-${(iso.quiz?has_content)?then('info', 'default')} btn-active-pink dropdown-toggle dropdown-toggle-icon" data-toggle="dropdown" type="button">
                                            Quiz <i class="dropdown-caret"></i>
                                        </button>
                                        <ul class="dropdown-menu">
                                            <li><a href="/iso-14155/${iso.id}/quiz"><i class="fa fa-pencil"></i> Edit</a></li>
                                            <li><a href="/iso-14155/${iso.id}/quiz/upload"><i class="fa fa-upload"></i> Template Upload</a></li>
                                            <#if iso.quiz?has_content>
                                                <li class="divider"></li>
                                                <li><a href="/iso-14155/${iso.id}/quiz/test"><i class="fa fa-check-square-o"></i> Test</a></li>
                                            </#if>
                                        </ul>
                                    </div>
                                    <!--===================================================-->
                                <#else>
                                    -
                                </#if>
                            </td>
                        </@security.authorize>
                        <td class="text-center"><span class="text-info">${iso.trainingDate}</span></td>
                        <td class="text-center"><span class="text-muted">${iso.createdDate?string('yyyy-MM-dd')}</span></td>
                        <td class="text-center">${iso.createdBy?default('System')}</td>
                        <@security.authorize access="hasAnyAuthority('ADMIN')">
                            <td class="text-center">
                                <#if iso.training>
                                    <#if iso.active>
                                        <button id="expandBtn" class="btn btn-primary btn-xs" data-id="${iso.id}"
                                                data-start="${iso.startDate?string('yyyy-MM-dd')}" data-date="${iso.trainingDate}"
                                                data-end="${iso.endDate?string('yyyy-MM-dd')}" data-name="${iso.title}">
                                            Period Expand
                                        </button>
                                    <#else>
                                        <button id="activeBtn" class="btn btn-purple btn-xs" data-id="${iso.id}"
                                                data-date="${iso.trainingDate}" data-attendee="${iso.attendee}" data-correct="${iso.correctCount}"
                                                data-test="${iso.quiz?has_content?then('Y', 'N')}">
                                            Activate
                                        </button>
                                    </#if>
                                <#else>
                                    -
                                </#if>
                            </td>
                        </@security.authorize>
                    </tr>
                </#list>
                <form method="post" id="activeForm" action="/iso-14155/active">
                    <input type="hidden" name="_method" value="put" />
                    <input type="hidden" id="activeIsoId" name="isoId" />
                </form>

                <form method="post" id="expandForm" action="/iso-14155/expand">
                    <input type="hidden" name="_method" value="put" />
                    <input type="hidden" id="expandIsoId" name="isoId" />
                    <input type="hidden" id="addDays" name="addDays" />
                </form>
            <#else>
                <tr>
                    <td colspan="5" class="text-center">등록된 ISO-14155가 없습니다.</td>
                </tr>
            </#if>
            </tbody>
        </table>
        <#--        </div>-->
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