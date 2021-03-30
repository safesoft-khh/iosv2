<#import "/spring.ftl" as spring />
<html>
<head>
    <title>Admin > ${type.name()}</title>
    <script src="/static/js/jquery.mask.min.js"></script>
    <link href="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.css" rel="stylesheet">
    <script src="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.js"></script>
    <#--    <link href="/static/plugins/chosen/chosen.min.css" rel="stylesheet">-->
    <#--    <script src="/static/plugins/chosen/chosen.jquery.min.js"></script>-->
    <link href="/static/plugins/select2/css/select2.min.css" rel="stylesheet">
    <script src="/static/plugins/select2/js/select2.min.js"></script>

    <!--Bootbox Modals [ OPTIONAL ]-->
    <script src="/static/plugins/bootbox/bootbox.min.js"></script>

    <#if type == 'SOP' && (CategoryList?has_content == false || CategoryList?size == 0)>
        <script>
            alert('SOP Category 정보가 존재하지 않습니다. Category 관리 페이지로 이동 합니다.');
            location.href = '/admin/sop/category';
        </script>
    <#elseif type == 'RF' && (sopList?has_content == false || sopList?size == 0)>
        <script>
            alert('SOP 등록 후 '+${formName}+' 등록을 진행해 주세요.');
            history.back();
        </script>
    </#if>
    <#assign readonly = ''/>
    <#if (action?has_content && action == 'revision') || documentVersion.parentVersion?has_content || (hasChild?has_content && hasChild == true)>
        <#assign readonly = " readonly='readonly'"/>
    </#if>
    <script>
        $(document).ready(function() {
            $("#document\\.documentNo").mask('${(type == 'SOP')?then('000', '00')}', {placeholder:'${(type == 'SOP')?then('000', '00')}'});
            <#if type == 'SOP'>
            $('#document\\.category\\.shortName').select2({
                placeholder: "Select a Category"
            });
            <#if readonly?has_content>
            $('#document\\.category\\.shortName').select2({disabled:true});
            </#if>

            $("#document\\.category\\.shortName").change(function() {
                var categoryId = $(this).val();
                var documentNo = $("#document\\.documentNo").val();

                $("#document\\.docId").val("${sopPrefix}" + categoryId + documentNo);
            });
            $("#document\\.documentNo").keyup(function() {
                var categoryId = $("#document\\.category\\.shortName").val();
                var documentNo = $(this).val();

                $("#document\\.docId").val("${sopPrefix}" + categoryId + documentNo);
            });
            <#else>
            $('#document\\.sop\\.docId').select2({
                placeholder: "Select a SOP"
            });
            <#if readonly?has_content>
            $('#document\\.sop\\.docId').select2({disabled:true});
            </#if>
            $("#document\\.sop\\.docId").change(function() {
                var prefix = $(this).val();
                var documentNo = $("#document\\.documentNo").val();

                $("#document\\.docId").val(prefix + "${formPrefix}" + documentNo);
            });
            $("#document\\.documentNo").keyup(function() {
                var prefix = $("#document\\.sop\\.docId").val();
                var documentNo = $(this).val();

                $("#document\\.docId").val(prefix + "${formPrefix}" + documentNo);
            });
            </#if>

            $("#document\\.documentNo").trigger("keyup");

            $('#effectiveDate').datepicker({format:'yyyy-mm-dd', autoclose:true, clearBtn:true, todayBtn: "linked", todayHighlight: true});

            <#if type == 'SOP'>
            $('#jdIds').select2({allowClear: true});
            $("#trainingAll").change(function() {
                if($(this).is(":checked")) {
                    $("#jdList").hide();
                    $('#jdIds').select2({disabled: true}).val(null).trigger("change");
                } else {
                    $("#jdList").show();
                    $('#jdIds').select2({disabled:false});
                }
            });

            $("#trainingAll").trigger("change");
            </#if>
        });
    </script>
</head>
<body>


<div class="panel">
    <div class="panel-heading">
        <h3 class="panel-title">${type.name()}</h3>
    </div>
    <form class="form-horizontal" method="post" enctype="multipart/form-data" id="documentForm">
        <@spring.formHiddenInput "documentVersion.id"/>
        <@spring.formHiddenInput "documentVersion.document.type"/>
        <div class="panel-body">
            <#if type == 'SOP' || type == 'ISO'>
                <div class="form-group">
                    <label class="col-sm-3 control-label">Category</label>
                    <div class="col-sm-9">
                        <@spring.formSingleSelect "documentVersion.document.category.shortName" CategoryList "class='form-control'${readonly}"/>
                    </div>
                </div>
            <#else>
                <div class="form-group">
                    <label class="col-sm-3 control-label">SOP</label>
                    <div class="col-sm-9">
                        <@spring.formSingleSelect "documentVersion.document.sop.docId" sopList "class='form-control'${readonly}"/>
                    </div>
                </div>
            </#if>
            <div class="form-group">
                <#if type == 'RF'>
                    <label class="col-sm-3 control-label">${formName} No</label>
                <#else>
                    <label class="col-sm-3 control-label">${type.name()} No</label>
                </#if>

                <div class="col-sm-9">
                    <@spring.formInput "documentVersion.document.documentNo" "class='form-control' placeholder='${type.name()} No'${readonly}"/>
                    <@spring.showErrors "" "color:red;"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-3 control-label">Document Id</label>
                <div class="col-sm-9">
                    <@spring.formInput "documentVersion.document.docId" "class='form-control' placeholder='Document Id' readonly='readonly'"/>
                    <@spring.showErrors "" "color:red;"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-3 control-label">Title</label>
                <div class="col-sm-9">
                    <@spring.formInput "documentVersion.document.title" "class='form-control' placeholder='Title'${readonly}"/>
                    <@spring.showErrors "" "color:red;"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-3 control-label">Version</label>
                <div class="col-sm-9">
                    <@spring.formInput "documentVersion.version" "class='form-control' placeholder='Version'" "text"/>
                    <@spring.showErrors "" "color:red;"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-3 control-label">Effective Date</label>
                <div class="col-sm-9">
                    <div class="input-group date">
                        <@spring.formInput "documentVersion.effectiveDate" "class='form-control' placeholder='Effective Date'"/>
                        <span class="input-group-addon"><i class="pli-calendar-4"></i></span>
                    </div>
                    <@spring.showErrors "<br/>" "color:red;"/>
                </div>
            </div>
            <#if type == 'SOP'>
                <div class="form-group">
                    <label class="col-sm-3 control-label">SOP Document File</label>
                    <div class="col-sm-9">
                        <#assign accept = '.pdf'/>
                        <@spring.formInput "documentVersion.uploadSopDocFile" "class='form-control' placeholder='${type.name()} Document File' accept='${accept}'" "file"/>
                        <@spring.showErrors "" "color:red;"/>
                    </div>
                </div>
            <#else>
                <div class="form-group">
                    <label class="col-sm-3 control-label">${formName} Document File(ENG)</label>
                    <div class="col-sm-9">
                        <#assign accept = '.xls,.xlsx,.doc,.docx,.hwp'/>
                        <@spring.formInput "documentVersion.uploadRfEngFile" "class='form-control' placeholder='${formName} Document File(ENG)' accept='${accept}'" "file"/>
                        <@spring.showErrors "" "color:red;"/>
                    </div>
                </div>
                <div id="engHwpPdf" class="form-group" style="display:none;">
                    <label class="col-sm-3 control-label">View PDF File (ENG)</label>
                    <div class="col-sm-9">
                        <#assign accept = '.pdf'/>
                        <@spring.formInput "documentVersion.uploadHwpEngPdfFile" "class='form-control' placeholder='${formName} Document File(ENG)' accept='${accept}'" "file"/>
                        <small style="color:red;">해당 입력 창에 PDF 파일을 업로드 하지 않았을 경우, 한글 문서(hwp)에 대한 뷰어가 적용되지 않습니다.</small>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label">${formName} Document File(KOR)</label>
                    <div class="col-sm-9">
                        <#assign accept = '.xls,.xlsx,.doc,.docx,.hwp'/>
                        <@spring.formInput "documentVersion.uploadRfKorFile" "class='form-control' placeholder='${formName} Document File(KOR)' accept='${accept}'" "file"/>
                        <@spring.showErrors "" "color:red;"/>
                    </div>
                </div>
                <div id="korHwpPdf" class="form-group" style="display:none;">
                    <label class="col-sm-3 control-label">View PDF File (KOR)</label>
                    <div class="col-sm-9">
                        <#assign accept = '.pdf'/>
                        <@spring.formInput "documentVersion.uploadHwpKorPdfFile" "class='form-control' placeholder='${formName} Document File(ENG)' accept='${accept}'" "file"/>
                        <small style="color:red;">해당 입력 창에 PDF 파일을 업로드 하지 않았을 경우, 한글 문서(hwp)에 대한 뷰어가 적용되지 않습니다.</small>
                    </div>
                </div>
            </#if>
            <#if type == 'SOP'>
                <div class="form-group">
                    <label class="col-sm-3 control-label">교육 대상(ROLE)</label>
                    <div class="col-sm-9">
                        <div class="row">
                            <div class="col-sm-2">
                                <@spring.formCheckbox "documentVersion.trainingAll" "class='magic-checkbox'"/><label for="trainingAll">전체</label>
                            </div>
                            <div class="col-sm-10" id="jdList">
                                <@spring.formMultiSelect "documentVersion.jdIds" jobDescriptionMap "class='form-control'"/>
                                <@spring.showErrors "" "color:red;"/>
                            </div>
                        </div>
                    </div>
                </div>
            </#if>
            <#if action?has_content && action == 'revision'>
                <div class="form-group">
                    <label class="col-sm-3 control-label">Status</label>
                    <div class="col-sm-9">
                        <@spring.formSingleSelect "documentVersion.status" statusMap "class='form-control'"/>
                        <small class="text-info"><i class="fa fa-exclamation-circle"></i> 현재 선택한 상태(Status)로 신규 버전이 추가 되며, Current 상태로 추가시 기존 버전은 Superseded 상태로 변경 됩니다.</small>
                    </div>
                </div>
            </#if>
            <div class="form-group">
                <label class="col-sm-3 control-label">알림 발송 여부</label>
                <div class="col-sm-9">
                    <div class="input-group date">
                        <@spring.formCheckbox "documentVersion.notification" "class='magic-checkbox'"/><label for="notification">발송</label>
                        <span class="input-group-addon"><i class="pli-calendar-4"></i></span>
                    </div>
                    <@spring.showErrors "<br/>" "color:red;"/>
                </div>
            </div>

        </div>
        <div class="panel-footer text-right">
            <a class="btn btn-link" href="/admin/${type}/management/${documentVersion.status.name()?lower_case}"><i class="fa fa-times"></i> 취소</a>
            <button class="btn btn-primary" type="submit" id="submitBtn" data-save-text="Saving.." data-loading-text="문서 변환 중.. 잠시만 기다려 주세요."><i class="fa fa-check"></i> 등록</button>
        </div>
    </form>
    <!--===================================================-->
    <!--End Horizontal Form-->

</div>
<script>
    $(document).ready(function() {
        $("#documentForm").submit(function(e) {
            $("#submitBtn").attr("disabled", "disabled");
            var $btn = $("#submitBtn").button('save');
            setTimeout(function() {
                $btn.button('loading');
            }, 1500);
        });


        $("#uploadRfEngFile").change(function(e){
            if ($(this).val().indexOf('.hwp') > -1) {
                $("#engHwpPdf").css('display', '');
            } else {
                $("#engHwpPdf").css('display', 'none');
                $("#uploadHwpEngPdfFile").val('');
            }
        });

        $("#uploadRfKorFile").change(function(e){
            if ($(this).val().indexOf('.hwp') > -1) {
                $("#korHwpPdf").css('display', '');
            } else {
                $("#korHwpPdf").css('display', 'none');
                $("#uploadHwpKorPdfFile").val('');
            }
        });

    });
</script>
</body>
</html>