<#import "/spring.ftl" as spring />
<html>
<head>
    <title>Off-line Training 등록 요청</title>
    <script src="/static/plugins/chosen/chosen.jquery.min.js"></script>
    <link href="/static/plugins/chosen/chosen.min.css" rel="stylesheet">
    <link href="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.css" rel="stylesheet">
    <script src="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.js"></script>
<#--    <script src="/static/plugins/bootstrap-tagsinput/bootstrap-tagsinput.min.js"></script>-->
<#--    <link href="/static/plugins/bootstrap-tagsinput/bootstrap-tagsinput.min.css" rel="stylesheet">-->

</head>
<body>


<div class="col-lg-12">
    <div class="panel">
        <div class="panel-heading">
            <h3 class="panel-title">Off-line Training</h3>
        </div>
        
        <!-- BASIC FORM ELEMENTS -->
        <!--===================================================-->
        <form class="panel-body form-horizontal form-padding" id="offlineForm" method="post">
            <input type="hidden" name="_method" id="_method">
            <input type="hidden" name="selectedId" id="selectedId">
            <input type="hidden" name="deselectedId" id="deselectedId">
            <!--Text Input-->
            <div class="form-group">
                <label class="col-md-3 control-label" for="demo-text-input">Training Date</label>
                <div class="col-md-9">
<#--                    <input type="text" id="demo-text-input" class="form-control" placeholder="Text">-->
                    <@spring.formInput "offlineTraining.trainingDate" "class='form-control'"/>
                    <@spring.showErrors "" "color:red;"/>
<#--                    <small class="help-block">(DD-MMM-YYYY)</small>-->
                </div>
            </div>

            <!--Email Input-->
            <div class="form-group">
                <label class="col-md-3 control-label">Organization</label>
                <div class="col-md-9">
<#--                    <input type="email" id="demo-email-input" class="form-control" placeholder="Enter your email">-->
                    <@spring.formInput "offlineTraining.organization" "class='form-control' placeholder='Organization'"/>
                    <@spring.showErrors "" "color:red;"/>
<#--                    <small class="help-block">(Academy, Self-training, etc.)</small>-->
                </div>
            </div>

            <!--Password-->
            <div class="form-group">
                <label class="col-md-3 control-label" for="demo-password-input">Training SOP</label>
                <div class="col-md-9">
                    <table class="table table-bordered">
                        <thead>
                        <tr class="bg-gray-light">
                            <th class="col-xs-1">No.</th>
                            <th>SOP</th>
                            <th class="col-xs-2">Training Hr.</th>
                        </tr>
                        </thead>
                        <tbody>
                        <#if offlineTraining.offlineTrainingDocuments?has_content>
                        <#list offlineTraining.offlineTrainingDocuments as sop>
                        <tr>
                            <td>${sop?index + 1}</td>
                            <td>${sop.documentVersion.document.docId} ${sop.documentVersion.document.title} v${sop.documentVersion.version}</td>
                            <td>
                                <@spring.formInput "offlineTraining.offlineTrainingDocuments[${sop?index}].hour" "class='form-control input-sm' step='0.1' min='0.1'" "number"/>
                                <@spring.showErrors "" "color:red;"/>
<#--                                <input type="number" class="form-control input-sm"/>-->
                            </td>
                        </tr>
                        </#list>
<#--                        <#else>-->
<#--                        <tr>-->
<#--                            <td colspan="3" class="text-center">-->
<#--&lt;#&ndash;                                <span class="text-danger"> <i class="fa fa-exclamation-circle"></i> SOP를 선택해 주세요.</span>&ndash;&gt;-->
<#--                            </td>-->
<#--                        </tr>-->
                        </#if>
                        <tr>
                            <td colspan="3">
                                <@spring.formMultiSelect "offlineTraining.sopIds" sopMap "class='form-control'"/>
                                <@spring.showErrors "" "color:red;"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>

                </div>
            </div>

            <div class="form-group">
                <label class="col-md-3 control-label">Attendee</label>
                <div class="col-md-9">
                    <@spring.formMultiSelect "offlineTraining.attendees" userMap "class='form-control'"/>
                    <@spring.showErrors "" "color:red;"/>
                    <small class="help-block">* 오프라인 교육 이력은 관리자 확인 후 반영 됩니다.</small>
                </div>
            </div>
        </form>
        <div class="panel-footer text-right">
            <button class="btn btn-primary" type="button" id="submitBtn"><i class="fa fa-send-o"></i> 등록 요청</button>
        </div>
        <!--===================================================-->
        <!-- END BASIC FORM ELEMENTS -->


    </div>
</div>

<script>
    $(function() {
        $('#sopIds,#attendees').chosen({width:'100%'});

        $("#sopIds").on("change", function(evt, params) {
           if(params.selected) {
               $("#selectedId").val(params.selected);
           } else if(params.deselected) {
               $("#deselectedId").val(params.deselected);
           }
           $("#_method").val("PUT");
           $("#offlineForm").submit();
        });

        $("#submitBtn").click(function() {
           $("#offlineForm").submit();
        });

        $('#trainingDate').datepicker({format:'yyyy-mm-dd', autoclose:true, clearBtn:true, todayBtn: "linked", todayHighlight: true});
    });
</script>
</body>
</html>