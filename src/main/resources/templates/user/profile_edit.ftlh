<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<@security.authentication property="principal" var="user"/>
<#import "/spring.ftl" as spring />
<html>
<head>
    <title>ISO Management System > User > Profile</title>
    <script src="/static/js/jquery.mask.min.js"></script>
    <link href="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.css" rel="stylesheet">
    <script src="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.js"></script>
    <link href="/static/plugins/select2/css/select2.min.css" rel="stylesheet">
    <script src="/static/plugins/select2/js/select2.min.js"></script>

    <script>
        $(document).ready(function() {
            $("#birthDate").datepicker({format:'yyyy-mm-dd', autoclose:true, clearBtn:true, todayBtn: "linked", todayHighlight: true, "endDate":new Date()});
            let departmentId = '${account.department?has_content?then(account.department.id, '')}';

            //초기 화면 전환시 세팅
            departmentSelect($("#deptSelect").val());

            let exists = false;
            $("#teamSelect option").each(function(){
                if (this.value == departmentId) {
                    exists = true;
                }
            })

            if(exists){
                $("#teamSelect").val(departmentId);
                $("#department").val(departmentId);
            }

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
    <div class="panel-heading">
        <h3 class="panel-title">내부 사용자 수정</h3>
    </div>
    <form class="form-horizontal" method="post">
        <div class="panel-body">
            <div class="form-group">
                <label class="col-sm-3 control-label">이름*</label>
                <div class="col-sm-9">
                    <@spring.formInput "account.name" "class='form-control'"/>
                    <@spring.showErrors "" "color:red;"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-3 control-label">이름 (영문명)</label>
                <div class="col-sm-9">
                    <@spring.formInput "account.engName" "class='form-control'"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-3 control-label">부서/팀</label>
                <div class="col-sm-9">
                    <@spring.formHiddenInput "account.department" "class='form-control'"/>
                    <div class="col-sm-6">
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
                    </div>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-3 control-label">생년월일</label>
                <div class="col-sm-9">
                    <@spring.formInput "account.birthDate" "class='form-control'"/>
                    <@spring.showErrors "" "color:red;"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-3 control-label">연락처</label>
                <div class="col-sm-9">
                    <@spring.formInput "account.phone" "class='form-control'"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-3 control-label">email</label>
                <div class="col-sm-9">
                    <@spring.formInput "account.email" "class='form-control'"/>
                    <@spring.showErrors "" "color:red;"/>
                </div>
            </div>
        </div>
        <div class="panel-footer text-right">
            <a class="btn btn-danger" href="/user/profile"><i class="fa fa-times"></i> 취소</a>
            <button class="btn btn-primary" type="submit" id="submitBtn" data-save-text="Saving.." data-loading-text="정보 수정 중.. 잠시만 기다려 주세요."><i class="fa fa-check"></i>수정</button>
        </div>
    </form>
    <!--===================================================-->
    <!--End Horizontal Form-->
</div>
</body>
</html>
