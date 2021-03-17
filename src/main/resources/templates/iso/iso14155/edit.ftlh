<#import "/spring.ftl" as spring />
<html>
<head>
    <title>ISO 14155 작성</title>
    <link href="/static/plugins/summernote/summernote.min.css" rel="stylesheet">
    <script src="/static/plugins/summernote/summernote.min.js"></script>
    <link href="/static/plugins/dropzone/dropzone.min.css" rel="stylesheet">
    <script src="/static/plugins/dropzone/dropzone.min.js"></script>
    <link href="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.css" rel="stylesheet">
    <script src="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.js"></script>

    <!-- Mask -->
    <script src="/static/js/jquery.mask.min.js"></script>

    <script>
        $(document).ready(function () {
            if ($('#content').length) {
                $('#content').summernote({
                    height: 430
                    , focus: true
                    , toolbar: [
                        // [groupName, [list of button]]
                        ['font2', ['fontname', 'fontsize', 'height']],
                        ['color', ['color']],
                        ['style', ['bold', 'italic', 'underline', 'clear']],
                        ['font1', ['strikethrough', 'superscript', 'subscript']],
                        ['table', ['table']],
                        ['para', ['ul', 'ol', 'paragraph']],
                        ['insert', ['link', 'picture', 'video', 'hr']]
                    ]
                    // airMode: true
                });
            }

            $('#topViewEndDate').datepicker({
                format: 'yyyy-mm-dd',
                autoclose: true,
                clearBtn: true,
                todayBtn: "linked",
                todayHighlight: true,
                zIndexOffset: 1000,
                startDate: new Date()
            });
            $('#startDate').datepicker({
                format: 'yyyy-mm-dd',
                autoclose: true,
                clearBtn: true,
                todayBtn: "linked",
                todayHighlight: true,
                zIndexOffset: 1000,
                startDate: new Date()
            });
            $('#endDate').datepicker({
                format: 'yyyy-mm-dd',
                autoclose: true,
                clearBtn: true,
                todayBtn: "linked",
                todayHighlight: true,
                zIndexOffset: 1000,
                startDate: new Date()
            });

            $('#userIds').select2({width: '100%'});

            $('#correctCount').mask('00');

            // $('#dropzone').dropzone({
            //     //url: '/file/post',
            //     autoProcessQueue: false,
            //     addRemoveLinks: true,
            //     maxFiles: 1,
            //     init: function() {
            //         var myDropzone = this;
            //         myDropzone.on('maxfilesexceeded', function(file) {
            //             this.removeAllFiles();
            //             this.addFile(file);
            //         });
            //     }
            // });\

            $("#training").change(function () {
                if ($(this).is(":checked")) {
                    $("#formTraining").css("display", "block");
                } else {
                    $("#formTraining").css("display", "none");
                }
            });

            $("#trainingAll").change(function (e) {
                if ($(this).is(":checked")) {
                    $("#userIdArea").css("display", "none");
                    ``
                    $("#userIds").val("").trigger("change");
                } else {
                    $("#userIdArea").css("display", "block");
                }
            });

            $("#isoForm").submit(function (e) {
                //Submit 하기 직전 disabled 상태 해제.
                $("#training").attr("disabled", false);

                var $btn = $("#submitBtn").button('save');
                setTimeout(function () {
                    $btn.button('loading');
                }, 1500);
            });
        });
    </script>
</head>
<body>
<!-- Contact Toolbar -->
<!---------------------------------->

<#--<div class="row pad-btm">-->
<#--    <div class="col-sm-6 col-sm-offset-6 toolbar-right text-right">-->
<#--&lt;#&ndash;        <button class="btn btn-default">Preview</button>&ndash;&gt;-->
<#--&lt;#&ndash;        <button class="btn btn-default">Draft</button>&ndash;&gt;-->
<#--        <button class="btn btn-primary">Save</button>-->
<#--    </div>-->
<#--</div>-->
<!---------------------------------->
<form role="form" method="post" id="isoForm" enctype="multipart/form-data">
    <div class="row">
        <div class="col-sm-9">
            <div class="panel">
                <div class="panel-body">
                    <@spring.formHiddenInput "iso.id"/>
                    <!-- Edit Case -->
                    <#assign disabled = iso.id?has_content?then('disabled', '') />
                    <div class="form-group">
                        <@spring.formCheckbox "iso.training" "class='form-checkbox' ${disabled}" />
                        <label class="control-label" for="training">Training 등록</label>
                    </div>

                    <!-- Edit인데 training 체크를 안한 경우일때만 예외, -->
                    <#if !(iso.id?has_content && iso.training == false) >
                        <div id="formTraining" style="display:${iso.training?then('block', 'none')};">
                            <div class="row">
                                <div class="col-lg-2 form-group">
                                    <label class="control-label" for="trainingAll">교육 참석 대상자</label>
                                </div>
                                <div class="col-lg-1 form-group">
                                    <@spring.formCheckbox "iso.trainingAll" "class='form-checkbox'" />
                                    <label class="control-label" for="trainingAll">전부</label>
                                </div>
                                <div class="col-lg-9 form-group">
                                    <div id="userIdArea" style="display:${iso.trainingAll?then('none', 'block')}">
                                        <@spring.formMultiSelect "iso.userIds" userMap "class='form-control'" />
                                        <@spring.showErrors "" "color:red;"/>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-lg-3 form-group">
                                    <label class="control-label" for="correctCount">합격 문항 개수</label>
                                </div>
                                <div class="col-lg-9 form-group">
                                    <@spring.formInput "iso.correctCount" "class='form-control' placeholder='최대 30 문항까지 입력'" "number" />
                                    <@spring.showErrors "" "color:red;"/>
                                </div>
                            </div>
                            <div class="row">
                                <label class="control-label col-lg-3" style="margin-top: 7px;">교육 기간</label>
                                <div class="form-group col-lg-4">
                                    <@spring.formInput "iso.startDate" "class='form-control' placeholder='Start Date'" />
                                    <@spring.showErrors "" "color:red;"/>
                                </div>
                                <div class="form-group col-lg-1 text-center">
                                    <span class="text-2x">~</span>
                                </div>
                                <div class="form-group col-lg-4">
                                    <@spring.formInput "iso.endDate" "class='form-control' placeholder='End Date'" />
                                    <@spring.showErrors "" "color:red;"/>
                                </div>
                            </div>
                        </div>
                    </#if>
                    <div class="form-group">
                        <@spring.formInput "iso.title" "class='form-control input-lg' placeholder='Title'"/>
                        <@spring.showErrors "" "color:red;"/>
                    </div>

                    <div class="form-group">
                        <@spring.formTextarea "iso.content" "class='form-control'"/>
                        <@spring.showErrors "" "color:red;"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-3">
            <div class="panel">
                <div class="panel-body">
                    <p class="text-main text-bold text-uppercase">첨부파일(ISO)</p>
                    <#if iso.attachFiles?has_content>
                        <div class="form-group">
                            <table class="table table-bordered">
                                <thead>
                                <tr>
                                    <th>#</th>
                                    <th>File</th>
                                </tr>
                                </thead>
                                <tbody>
                                <#list iso.attachFiles as attachFile>
                                    <tr>
                                        <td>${attachFile?index + 1}</td>
                                        <td>${attachFile.originalFileName}</td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </#if>
                    <div class="form-group">
                        <input name="uploadingFile" class="form-control" type="file" accept=".pdf">
                        <@spring.formHiddenInput "iso.attachFiles" />
                        <@spring.showErrors "" "color:red;"/>
                    </div>
                    <hr>
                    <p class="text-main text-bold text-uppercase">상단공지</p>
                    <div class="form-horizontal">
                        <div class="form-group">
                            <label class="col-sm-5 control-label text-left">종료일</label>
                            <div class="col-sm-7">
                                <div class="input-group date">
                                    <@spring.formInput "iso.topViewEndDate" "class='form-control'"/>
                                    <span class="input-group-addon"><i class="pli-calendar-4"></i></span>
                                </div>
                                <@spring.showErrors "" "color:red;"/>
                            </div>
                        </div>
                    </div>
                    <hr/>
                    <div class="text-right">
                        <button type="button" class="btn btn-link" onclick="history.back();">Cancel</button>
                        <button type="submit" class="btn btn-primary" id="submitBtn" data-save-text="Saving.."
                                data-loading-text="문서 변환 중.. 잠시만 기다려 주세요."> Save
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</form>
</body>
</html>
