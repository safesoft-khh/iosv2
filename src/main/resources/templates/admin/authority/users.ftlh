<#import "/common/pagination.ftl" as pagination />
<#import "/spring.ftl" as spring />
<html>
<head>
    <title>Admin > User Management > 사용자 관리</title>

    <!-- select2 -->
    <link href="/static/plugins/select2/css/select2.min.css" rel="stylesheet">
    <!-- select2 -->
    <script src="/static/plugins/select2/js/select2.min.js"></script>

    <!-- Data Table -->
    <link href="/static/plugins/datatables/media/css/jquery.dataTables.min.css" rel="stylesheet">
    <link href="/static/plugins/datatables/media/css/dataTables.bootstrap4.min.css" rel="stylesheet">
    <!-- Data Table -->
    <script src="/static/plugins/datatables/media/js/jquery.dataTables.min.js"></script>
    <script src="/static/plugins/datatables/media/js/dataTables.bootstrap4.min.js"></script>

    <!--Bootbox Modals [ OPTIONAL ]-->
    <script src="/static/plugins/bootbox/bootbox.min.js"></script>


    <style>
        .select2-container--open {
            z-index: 9999999
        }

        .role-rquest-content {
            padding: 10px;
        }

        .modal .modal-fullsize {
            width: 80%;
        }
    </style>

    <script>
        //Training 진행 여부
        $(document).ready(function () {

            $("button[name='trainingBtn']").click(function () {
                $.ajax({
                    url: '/admin/authority/users/' + $(this).data("id") + '/training',
                    method: 'post',
                    data: {"_method": "put", r: Math.random()},
                    success: function (res) {
                        if (res.result == 'success') {
                            $("#trainingBtn" + res.id).removeClass('btn-' + (res.className == 'default' ? 'success' : 'default'));
                            $("#trainingBtn" + res.id).addClass('btn-' + res.className);

                            $.niftyNoty({
                                type: 'info',
                                container: 'floating',
                                message: '[' + res.name + '] 교육 진행 상태가 변경 되었습니다.',
                                closeBtn: true,
                                floating: {
                                    position: 'top-right',
                                    animationIn: 'fadeInUp',
                                    animationOut: 'fadeOut'
                                },
                                focus: true,
                                timer: 1000
                            });
                        }
                    }
                });
            });

            //이메일 수신여부 확인
            $("button[name='receiveEmailBtn']").click(function () {
                $.ajax({
                    url: '/admin/authority/users/' + $(this).data("id") + '/receiveEmail',
                    method: 'post',
                    data: {"_method": "put", r: Math.random()},
                    success: function (res) {
                        if (res.result == 'success') {
                            $("#receiveEmailBtn" + res.id).removeClass('btn-' + (res.className == 'default' ? 'success' : 'default'));
                            $("#receiveEmailBtn" + res.id).addClass('btn-' + res.className);

                            $.niftyNoty({
                                type: 'info',
                                container: 'floating',
                                message: '[' + res.name + '] 이메일 수신 상태가 변경 되었습니다.',
                                closeBtn: true,
                                floating: {
                                    position: 'top-right',
                                    animationIn: 'fadeInUp',
                                    animationOut: 'fadeOut'
                                },
                                focus: true,
                                timer: 1000
                            });
                        }
                    }
                });
            });

            $("#deptSelect").change(function(){
                departmentSelect($(this).val());
            })

            $("#teamSelect").change(function(){
                let res = $(this).val();

                if(res == 'department') {
                    $("#department").val($("#deptSelect").val());
                }else {
                    $("#department").val(res);
                }
            })

            $(document).on("click", "#acceptBtn", function (e) {
                let id = $(this).data("id");
                let username = $(this).data("value");
                let JDList = String($(this).data("jds")).indexOf(",")!=-1?$(this).data("jds").split(','):$(this).data("jds");
                let pid = $(this).data("pdeptid");
                let deptId = $(this).data("deptid");

                $("#accept-modal #userId").val(id);
                $("#accept-modal .userTitle").html("[" + username + "]'s Information");

                //Job Description Setting
                $("#jdIds").val(JDList).trigger("change");

                if(pid != ''){ //pid가 있는 경우,
                    $("#deptSelect").val(pid).trigger("change");
                    $("#teamSelect").val(deptId).trigger("change");
                } else { //pid가 없는 경우
                    $("#deptSelect").val(deptId).trigger("change");
                }
            });

            //거절 버튼 입력 시
            $(document).on("click", "#rejectBtn", function (e) {
                let username = $(this).data("value");
                if (!confirm("유저 [" + username + "]의 회원가입 신청을 거절합니다. 계속 진행하시겠습니까?")) {
                    e.preventDefault();
                }
            });

            $('#jdIds').select2({width: '100%'});

            $('#roleRequests').dataTable({
                "responsive": true,
                "language": {
                    "decimal" : "",
                    "emptyTable" : "ROLE 신청 내역이 없습니다.",
                    "info" : "_START_ - _END_ (총 _TOTAL_ 명)",
                    "infoEmpty" : "0명",
                    "infoFiltered" : "(전체 _MAX_ 명 중 검색결과)",
                    "infoPostFix" : "",
                    "thousands" : ",",
                    "lengthMenu" : "_MENU_ 개씩 보기",
                    "loadingRecords" : "로딩중...",
                    "processing" : "처리중...",
                    "search" : "검색 : ",
                    "zeroRecords" : "검색된 데이터가 없습니다.",
                    "paginate" : {
                        "previous": '<i class="pli-arrow-left"></i>',
                        "next": '<i class="pli-arrow-right"></i>',
                        "first" : '<i class="pli-arrow-left-2"></i>',
                        "last" : '<i class="pli-arrow-right-2"></i>',
                    },
                    "aria" : {
                        "sortAscending" : " :  오름차순 정렬",
                        "sortDescending" : " :  내림차순 정렬"
                    }
                },
                "order":[[0, "desc"]],
                "columns": [
                    {"orderable": false},
                    {"orderable": false},
                    {"orderable": false},
                    {"orderable": false},
                    {"orderable": false},
                    {"orderable": false},
                    {"orderable": false},
                    {"orderable": false},
                    {"orderable": false}
                ]
            });

            //Role Request Button 처리
            $(document).on("click", "#roleAcceptBtn", function(){
                let id = $(this).data("value");
                let name = $(this).data("name");

                if (confirm("[" + name + "]의 Role 변경 신청을 수락합니다.\n계속 하시겠습니까?")) {
                    $("#roleRequestId").val(id);
                    $("#roleRequestAction").val("accept");
                    $("#roleRequestForm").submit();
                } else {
                    console.log("cancel");
                }
            });

            $("#roleRequestForm").on("submit", function(e){
               console.log("SUBMIT : ", $(this).attr("action"));
            });

            $(document).on("click", "#roleRejectBtn", function(){
                let id = $(this).data("value");
                let name = $(this).data("name");

                if (confirm("[" + name + "]의 Role 변경 신청을 거절합니다.\n계속 하시겠습니까?")) {
                    $("#roleRequestId").val(id);
                    $("#roleRequestAction").val("reject");
                    console.log($("#roleRequestForm").submit());
                }
            });

            $(document).on("click", "#pwReset", function(e){
                let name = $(this).data("value");
                if(!confirm("[" + name + "]님의 비밀번호를 초기화 합니다.\n계속 진행하시겠습니까?")) {
                    e.preventDefault();
                }
            })
        });

        function departmentSelect(value) {
            $("#department").val(value);
            $.ajax({
                url: "/signUp/ajax/department",
                method: 'post',
                data: {id: value},
                async: false,
                success: function (data) {
                    let teamSelect = $("#teamSelect");

                    if(data != '') {
                        teamSelect.attr("disabled", false);
                        teamSelect.html(data);
                    } else {
                        teamSelect.attr("disabled", true);
                        teamSelect.html(data);
                    }
                }
            });
        }

    </script>
</head>
<body>

<div class="panel">
    <!--Data Table-->
    <!--===================================================-->
    <div class="panel-body">
        <form id="roleRequestForm" action="/admin/authority/users/role_request" method="POST">
            <input type="hidden" name="_method" value="PUT"/>
            <input type="hidden" name="id" id="roleRequestId"/>
            <input type="hidden" name="action" id="roleRequestAction" />
        </form>
        <#if roleRequestList.totalElements gt 0>
            <div class="alert alert-info">
                <button class="close" data-dismiss="alert"><i class="pci-cross pci-circle"></i></button>
                <strong><i class="pli-bell"></i> ROLE 변경 신청</strong>이력이 확인 되었습니다. (<a href="#" class="alert-link" id="roleRequestBtn" data-toggle="modal" data-target="#role-request-modal" >확인 하기</a>)
            </div>
        </#if>

        <@pagination.bind users/>
        <div class="pad-btm">
<#--            <div class="pull-right">-->
<#--                <form method="post" action="/admin/authority/users/sync">-->
<#--                <button class="btn btn-info"><i class="pli-sync icon-lg"></i> 조직도 동기화</button>-->
<#--                </form>-->
<#--            </div>-->
            <form name="f" id="f" class="form-inline">
<#--                <#if deptList?has_content>-->
<#--                    <#assign pDeptCode = RequestParameters['deptCode']?default('')/>-->
<#--                    <div class="box-inline mar-btm">-->
<#--                        &lt;#&ndash;                        Team :&ndash;&gt;-->
<#--                        &lt;#&ndash;                        <div class="select">&ndash;&gt;-->
<#--                        <select name="deptCode" id="deptCode" class="form-control">-->
<#--                            <option value=""${pTeamCode?has_content?then('', ' selected')}>ALL</option>-->
<#--                            <#list deptList as dept>-->
<#--                                <option value="${dept['deptCode']}"${pDeptCode?has_content?then((pDeptCode == dept['deptCode'])?then(' selected', ''), '')}>${dept['deptName']}</option>-->
<#--                            </#list>-->
<#--                        </select>-->
<#--                        &lt;#&ndash;                        </div>&ndash;&gt;-->
<#--                    </div>-->
<#--                    <script>-->
<#--                        $("#deptCode").change(function() {-->
<#--                            $("#teamCode").val("");-->

<#--                            $("#f").attr("method", "get").submit();-->
<#--                        });-->
<#--                    </script>-->
<#--                </#if>-->
<#--                <#if teamList?has_content>-->
<#--                    <#assign pTeamCode = RequestParameters['teamCode']?default('')/>-->
<#--                    <div class="box-inline mar-btm">-->
<#--                        &lt;#&ndash;                        Team :&ndash;&gt;-->
<#--                        &lt;#&ndash;                        <div class="select">&ndash;&gt;-->
<#--                        <select name="teamCode" id="teamCode" class="form-control">-->
<#--                            <option value=""${pTeamCode?has_content?then('', ' selected')}>ALL</option>-->
<#--                            <#list teamList as team>-->
<#--                                <option value="${team['teamCode']}"${pTeamCode?has_content?then((pTeamCode == team['teamCode'])?then(' selected', ''), '')}>${team['teamName']}</option>-->
<#--                            </#list>-->
<#--                        </select>-->
<#--                        &lt;#&ndash;                        </div>&ndash;&gt;-->
<#--                    </div>-->
<#--                    <script>-->
<#--                        $("#teamCode").change(function() {-->
<#--                            $("#userList").val("");-->

<#--                            $("#f").attr("method", "get").submit();-->
<#--                        });-->
<#--                    </script>-->
<#--                </#if>-->
                <div class="box-inline mar-btm">
                    <input type="text" name="name" value="${RequestParameters['name']?default('')}" class="form-control" placeholder="사용자 명">
                </div>
                <div class="box-inline mar-btm">
                    <button class="btn btn-default" type="submit"><i class="fa fa-search"></i></button>
                </div>
            </form>
        </div>
        <div class="table-responsive">
            <table class="table table-hover table-bordered">
                <thead>
                <tr class="bg-gray-light">
                    <th class="text-center" style="width:80px">No.</th>
                    <th class="text-center">ID</th>
                    <th class="text-center">부서</th>
                    <th class="text-center">팀</th>
                    <th class="text-center">이름</th>
                    <th class="text-center">생년월일</th>
                    <th class="text-center">연락처</th>
                    <th class="text-center">E-mail</th>
                    <th class="text-center">교육 진행</th>
                    <th class="text-center">이메일 수신</th>
                    <th class="text-center">#Actions</th>
                </tr>
                </thead>
                <tbody>
                <#if users.content?has_content>
                    <#list users.content as t>
                        <tr>
                            <td class="text-center" style="vertical-align: middle;">${(users.totalElements - (users.size * users.number)) - t?index}</td>
                            <td class="text-center" style="vertical-align: middle;">${t.username}</td>
                            <td class="text-center" style="vertical-align: middle;">${t.deptName!''}</td>
                            <td class="text-center" style="vertical-align: middle;">${t.teamName!''}</td>
                            <td class="text-center" style="vertical-align: middle;">${t.name}</td>
                            <td class="text-center" style="vertical-align: middle;">${t.birthDate?has_content?then(t.birthDate?string('yyyy-MM-dd'), '')}</td>
                            <td class="text-center" style="vertical-align: middle;">${t.phone!''}</td>
                            <td class="text-center" style="vertical-align: middle;">${t.email!''}</td>
                            <td class="text-center">
                                <#if t.userStatus?has_content && t.userStatus == 'ACTIVE'>
                                    <button class="btn btn-icon btn-${t.training?then('success', 'default')} btn-circle" data-id="${t.id}" name="trainingBtn" id="trainingBtn${t.id}"><i class="fa fa-check"></i></button>
                                <#else>
                                    -
                                </#if>
                            </td>
                            <td class="text-center">
                                <#if t.userStatus?has_content && t.userStatus == 'ACTIVE'>
                                    <button class="btn btn-icon btn-${t.receiveEmail?then('success', 'default')} btn-circle" data-id="${t.id}" name="receiveEmailBtn" id="receiveEmailBtn${t.id}"><i class="fa fa-check"></i></button>
                                <#else>
                                    -
                                </#if>
                            </td>
                            <td class="text-center">
                                <#if t.userStatus?has_content && t.userStatus == 'ACTIVE'>
                                    <form method="POST" action="/admin/authority/users/pwReset/${t.id}">
                                        <a href="/admin/authority/users/${t.id}" class="btn btn-icon btn-info">Edit</a>
                                        <button id="pwReset" class="btn btn-icon btn-danger" data-value="${t.name}">Password Reset</button>
                                    </form>
                                <#elseif t.userStatus?has_content && t.userStatus == 'SIGNUP_REQUEST'>
                                    <form class="form-horizontal" method="POST">
                                        <input name="id" type="hidden" value="${t.id}" />
                                        <button class="btn btn-icon btn-info" type="button" id="acceptBtn" data-toggle="modal" data-id="${t.id}" data-value="${t.username}" data-jds="${t.commaJobIds}"
                                                data-pdeptid="<#if t.department?has_content && t.department.parentDepartment?has_content>${t.department.parentDepartment.id}</#if>" data-deptid="${t.department?has_content?then(t.department.id!'', '')}" data-target="#accept-modal">Accept</button>
                                        <button class="btn btn-icon btn-danger" id="rejectBtn" data-value="${t.username}" name="result" value="reject">Reject</button>
                                    </form>
                                <#elseif t.userStatus?has_content && t.userStatus == 'RETIREE'>
                                    <span class="badge badge-info text-2x">퇴사자</span>
                                </#if>
                            </td>
                        </tr>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="11" class="text-center">등록된 데이터가 없습니다.</td>
                    </tr>
                </#if>
                </tbody>
            </table>
        </div>
        <@pagination.default/>
    </div>
    <!--===================================================-->
    <!--End Data Table-->

    <!-- Sign Up Accept Modal -->
    <div class="modal fade" id="accept-modal" role="dialog" tabindex="-1" aria-labelledby="accept-modal"
         aria-hidden="true">
        <div class="modal-dialog modal-md">
            <div class="modal-content">
                <!--Modal header-->
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><i class="pci-cross pci-circle"></i></button>
                    <h4 class="modal-title">Accept Check</h4>
                </div>
                <form class="form-horizontal" method="POST">
                    <input name="id" id="userId" type="hidden" />
                    <!--Modal body-->
                    <div class="modal-body">
                        <div class="panel panel-bordered-primary panel-primary">
                            <div class="panel-heading">
                                <h4 class="text-light userTitle" style="margin:0 0; padding:7px 0 0 7px;"></h4>
                            </div>
                            <div class="panel-body">
                                <div class="row">
                                    <@spring.formHiddenInput "account.department" "class='form-control'"/>
                                    <label class="control-label">Department / Team* : </label>
                                    <div class="form-group">
                                        <div class="col-sm-6" style="padding-left: 0px">
                                            <select id="deptSelect" class="form-control">
                                                <#list departments as d>
                                                    <option value="${d.id}">${d.name}</option>
                                                </#list>
                                            </select>
                                        </div>
                                        <div class="col-sm-6" style="padding-right: 0px">
                                            <select id="teamSelect" class="form-control" disabled></select>
                                        </div>
                                    </div>
                                    <label class="control-label">ROLE</label>
                                    <div class="form-group">
                                        <@spring.formMultiSelect "account.jdIds" jobDescriptionMap "class='form-control'" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!--Modal footer-->
                    <div class="modal-footer">
                        <button class="btn btn-icon btn-info" name="result" value="accept">Accept</button>
                        <button data-dismiss="modal" class="btn btn-default" id="closeBtn" type="button">Close</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <!-- End Sign Up Accept Modal-->

    <!-- Role Accept Modal -->
    <div class="modal fade" id="role-request-modal" role="dialog" tabindex="-1" aria-labelledby="role-request-modal"
         aria-hidden="true">
        <div class="modal-dialog modal-fullsize">
            <div class="modal-content">
                <!--Modal header-->
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><i class="pci-cross pci-circle"></i>
                    </button>
                    <h4 class="modal-title">ROLE Requests List</h4>
                </div>
                <!--Modal body-->
                <div class="modal-body">
                    <div class="panel panel-bordered-primary role-rquest-content">
                        <div class="table-responsive">
                            <table id="roleRequests" class="table table-striped">
                                <thead>
                                <tr>
                                    <th class="text-center">No.</th>
                                    <th class="text-center">팀/부서</th>
                                    <th class="text-center">이름</th>
                                    <th class="text-center">변경 전 ROLE</th>
                                    <th class="text-center">변경 후 ROLE</th>
                                    <th class="text-center">사유</th>
                                    <th class="text-center">요청 일자</th>
                                    <th class="text-center">배정 일자</th>
                                    <th class="text-center">#Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <#if roleRequestList.content?has_content>
                                    <#list roleRequestList.content as roleRequest>
                                        <tr>
                                            <td class="text-center">${(roleRequestList.totalElements - (roleRequestList.size * roleRequestList.number)) - roleRequest?index}</td>
                                            <td class="text-center">${roleRequest.requester.teamDept}</td>
                                            <td class="text-center">${roleRequest.requester.name}</td>
                                            <td class="text-center"><span
                                                        class="label label-warning">${roleRequest.prevJobDescription}</span>
                                            </td>
                                            <td class="text-center"><span
                                                        class="label label-purple">${roleRequest.nextJobDescription}</span>
                                            </td>
                                            <td class="text-center">
                                                ${roleRequest.reason}
                                            </td>
                                            <td class="text-center">${roleRequest.requestDate?string('yyyy-MM-dd')}</td>
                                            <td class="text-center">${roleRequest.assignDate?string('yyyy-MM-dd')}</td>
                                            <td class="text-center">
                                                <button type="button" class="btn btn-info" id="roleAcceptBtn"
                                                        data-value="${roleRequest.id}"
                                                        data-name="${roleRequest.requester.name}">Accept
                                                </button>
                                                <button type="button" class="btn btn-danger" id="roleRejectBtn"
                                                        data-value="${roleRequest.id}"
                                                        data-name="${roleRequest.requester.name}">Reject
                                                </button>
                                            </td>
                                        </tr>
                                    </#list>
                                </#if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <!--Modal footer-->
                <div class="modal-footer">
                    <button data-dismiss="modal" class="btn btn-default" id="closeBtn" type="button">Close</button>
                </div>
            </div>
        </div>
    </div>
    <!-- End Role Accept Modal-->
</div><!-- end panel-->
</body>
</html>
