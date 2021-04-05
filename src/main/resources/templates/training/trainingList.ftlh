<#import "/common/pagination.ftl" as pagination />
<#setting locale="en_US">
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<html>
<head>
    <title>ISO Management System > Training > ${requirement.label} Training</title>
</head>
<body>


<div class="tab-base">
    <#if requirement == 'mandatory'>
    <ul class="nav nav-tabs">
        <li class="active">
            <a href="#">Ongoing</a>
        </li>
        <li>
            <a href="/training/sop/mandatory-training/completed">Completed</a>
        </li>
    </ul>
    </#if>
    <div class="tab-content">
    <!--Data Table-->
    <!--===================================================-->
        <@pagination.bind trainingMatrix/>
        <div class="table-responsive">
            <table class="table table-bordered table-hover">
                <thead>
                <tr class="bg-gray-light">
                    <th class="text-center">No.</th>
                    <th class="text-center">Document ID</th>
                    <th>Title</th>
                    <th class="text-center">Version</th>
                    <th class="text-center">Effective Date</th>
                    <#if requirement == 'mandatory'>
                    <th class="text-center">Training period</th>
                    </#if>
                    <th class="text-center">Progress</th>
                    <th class="text-center">Status</th>
<#--                    <th class="text-center">Complete date</th>-->
                    <th>#Actions</th>
                </tr>
                </thead>
                <tbody>
                <#if trainingMatrix.content?has_content>
                <#list trainingMatrix.content as t>
                    <tr<#if bgClass?has_content> class="${bgClass}"</#if>>
                        <td class="text-center">${(trainingMatrix.totalElements - (trainingMatrix.size * trainingMatrix.number)) - t?index}</td>
<#--                        <td class="text-center">${(trainingMatrix.size * trainingMatrix.number) + t?index + 1}</td>-->
                        <td class="text-center">${t.document.docId}</td>
                        <td>${t.document.title}<#if t.trainingPeriod.trainingType == 'REFRESH' || t.trainingPeriod.trainingType == 'RE_TRAINING'> <span class="badge badge-warning">${t.trainingPeriod.trainingType.label}</span></#if></td>
                        <td class="text-center">${t.documentVersion.version}</td>
                        <td class="text-center">${t.documentVersion.effectiveDate?string('yyyy.MM.dd')}</td>
                        <#if requirement == 'mandatory'>
                        <td class="text-center">
                            <span class="text-info"> ${t.userStartDate?string('yyyy.MM.dd')}~${t.userEndDate?string('yyyy.MM.dd')}</span>
                        </td>
                        </#if>
                        <td class="text-center">
                            <#assign percent = 0/>
                                <#if t.trainingLog?has_content>
                                    <#assign percent = (t.trainingLog.progressPercent?has_content)?then(t.trainingLog.progressPercent, 0)/>
                                </#if>
                                <div class="progress"><div style="width: ${percent}%;" class="progress-bar progress-bar-info">${percent}%</div></div>
                        </td>
                        <td class="text-center">
                            <#if t.trainingLog?has_content>
                                <span class="label label-${t.trainingLog.status.className}">${t.trainingLog.status.label}</span>
                            <#else>
                                <span class="label label-default">Not Started</span>
<#--                            <#elseif t.trainingLog?has_content == false && t.isOutOfPeriod() && requirement == "mandatory">-->
<#--                                <a class="btn btn-purple btn-xs" href="/approval/box/requester/SOP_Deviation_Report/new?trainingPeriodId=${t.trainingPeriod.id}&sopId=${t.documentVersion.id}"><i class="fa fa-external-link"></i> Deviation Report</a>-->
                            </#if>
                        </td>
<#--                        <td class="text-center"><#if t.trainingLog?has_content && t.trainingLog.completeDate?has_content>-->
<#--                            <span class="text-mint">${t.trainingLog.completeDate?string('yyyy.MM.dd')}</span>-->
<#--                        </#if></td>-->
                        <td>
                            <#if t.trainingLog?has_content>
                                <#if t.trainingLog.reportStatus == "REPORTED">
                                    <button class="btn btn-info btn-xs" type="button" href="#training-modal" data-toggle="modal" data-title="${t.document.title} v${t.documentVersion.version}" data-id="${t.documentVersion.id}" data-pid="${t.trainingPeriod.id}"<#if t.trainingLog?has_content> data-logid="${t.trainingLog.id}" data-page="${t.trainingLog.lastPageNo?default(0)}" data-percent="${t.trainingLog.progressPercent?default(0)}" data-time="${t.trainingLog.trainingTime?default(0)}"</#if> data-total="${(t.documentVersion.totalPage?has_content)?then(t.documentVersion.totalPage, 0)}" disabled="disabled"><i class="fa fa-search"></i> Training</button>
                                <#elseif t.trainingPeriod.trainingType == 'SELF' && t.isUserOutOfPeriod() && requirement == "mandatory" && t.trainingLog.status != "COMPLETED" && t.trainingLog.reportStatus != "APPROVED">
                                    <a class="btn btn-warning btn-xs" href="/approval/box/requester/SOP_Training_Deviation_Report/new?trainingPeriodId=${t.trainingPeriod.id}&sopId=${t.documentVersion.id}"><i class="fa fa-external-link"></i> SOP Deviation Report</a>
                                <#elseif t.trainingLog.status == "TEST_FAILED">
                                <button class="btn btn-warning btn-xs" type="button" href="#test-modal" data-toggle="modal" data-title="${t.document.title} v${t.documentVersion.version}" data-id="${t.documentVersion.id}" data-logid="${t.trainingLog.id}"><i class="fa fa-check-square-o"></i> Re-test</button>
                                <#elseif t.trainingLog.status == "TRAINING_COMPLETED">
                                    <button class="btn btn-info btn-xs" type="button" href="#test-modal" data-toggle="modal"<#if t.documentVersion.quiz?has_content == false>disabled="disabled"</#if> id="testBtn${t.trainingLog.id}" data-title="${t.document.title} v${t.documentVersion.version}" data-id="${t.documentVersion.id}" data-logid="${t.trainingLog.id}"><i class="fa fa-check-square-o"></i> Test</button>
                                <#elseif t.trainingLog.status == "PROGRESS">
                                    <button class="btn btn-info btn-xs" type="button" href="#training-modal" data-toggle="modal" data-title="${t.document.title} v${t.documentVersion.version}" data-id="${t.documentVersion.id}" data-pid="${t.trainingPeriod.id}"<#if t.trainingLog?has_content> data-logid="${t.trainingLog.id}" data-page="${t.trainingLog.lastPageNo?default(0)}" data-percent="${t.trainingLog.progressPercent?default(0)}" data-time="${t.trainingLog.trainingTime?default(0)}"</#if> data-total="${(t.documentVersion.totalPage?has_content)?then(t.documentVersion.totalPage, 0)}"<#if t.documentVersion.totalPage?has_content == false> disabled="disabled"</#if>><i class="fa fa-search"></i> Training</button>
                                <#elseif t.trainingLog.status != "COMPLETED" && t.trainingLog.reportStatus == "APPROVED">
                                    <button class="btn btn-info btn-xs" type="button" href="#training-modal" data-toggle="modal" data-title="${t.document.title} v${t.documentVersion.version}" data-id="${t.documentVersion.id}" data-pid="${t.trainingPeriod.id}"<#if t.trainingLog?has_content> data-logid="${t.trainingLog.id}" data-page="${t.trainingLog.lastPageNo?default(0)}" data-percent="${t.trainingLog.progressPercent?default(0)}" data-time="${t.trainingLog.trainingTime?default(0)}"</#if> data-total="${(t.documentVersion.totalPage?has_content)?then(t.documentVersion.totalPage, 0)}"<#if t.documentVersion.totalPage?has_content == false || (t.trainingLog?has_content == false && t.isOutOfPeriod() && requirement == "mandatory")> disabled="disabled"</#if>><i class="fa fa-search"></i> Training</button>
                                </#if>
                            <#else>
                                <#if t.trainingPeriod.trainingType == 'SELF' && t.isUserOutOfPeriod() && requirement == "mandatory">
                                <a class="btn btn-warning btn-xs" href="/approval/box/requester/SOP_Training_Deviation_Report/new?trainingPeriodId=${t.trainingPeriod.id}&sopId=${t.documentVersion.id}"><i class="fa fa-external-link"></i> SOP Deviation Report</a>
                                <#else>
                                    <button class="btn btn-info btn-xs" type="button" href="#training-modal" data-toggle="modal" data-title="${t.document.title} v${t.documentVersion.version}" data-id="${t.documentVersion.id}" data-pid="${t.trainingPeriod.id}"<#if t.trainingLog?has_content> data-logid="${t.trainingLog.id}" data-page="${t.trainingLog.lastPageNo?default(0)}" data-percent="${t.trainingLog.progressPercent?default(0)}" data-time="${t.trainingLog.trainingTime?default(0)}"</#if> data-total="${(t.documentVersion.totalPage?has_content)?then(t.documentVersion.totalPage, 0)}"<#if t.documentVersion.totalPage?has_content == false> disabled="disabled"</#if>><i class="fa fa-search"></i> Training</button>
                                </#if>
                            </#if>
                        </td>
                    </tr>
                </#list>
                <#else>
                    <tr>
                        <td colspan="${(requirement == 'mandatory')?then(9, 8)}" class="text-center">No Data</td>
                    </tr>
                </#if>
                </tbody>
            </table>
        </div>
        <@pagination.default/>
    </div>
    <!--===================================================-->
    <!--End Data Table-->

</div>
<form id="trainingLogForm" method="post">
    <input type="hidden" name="docVersionId" id="docVersionId">
    <input type="hidden" name="progressPercent" id="progressPercent">
    <input type="hidden" name="trainingPeriodId" id="trainingPeriodId">
    <input type="hidden" name="trainingTime" id="trainingTime">
    <input type="hidden" name="trainingLogId" id="trainingLogId">
    <input type="hidden" name="lastPageNo" id="lastPageNo">
</form>
<div class="modal fade" id="training-modal" role="dialog" tabindex="-1" aria-labelledby="training-modal" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">

            <!--Modal header-->
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><i class="pci-cross pci-circle"></i></button>
                <h4 class="modal-title">SOP Self Training : <span id="sopInfo"></span> </h4>
            </div>

            <!--Modal body-->
            <div class="modal-body">
                <div id="image-view"></div>
            </div>

            <!--Modal footer-->
            <div class="modal-footer">
                <div class="row">
                    <div class="col-xs-8 text-left">
                        <i class="fa fa-clock-o"></i> 경과 시간 : <span id="timer"></span>
                        <div class="progress" id="progressDiv"><div style="width: 0%;" class="progress-bar progress-bar-info">0%</div></div>
<#--                        진행율:<span id="progressPercentDiv"></span>-->

                    </div>
<#--                    <div class="col-xs-6">-->
<#--                        <div class="progress" id="progressDiv"><div style="width: 0%;" class="progress-bar progress-bar-info">75%</div></div>-->
<#--                    </div>-->
                    <div class="col-xs-4">

                        <button class="btn btn-default" id="prevBtn"><i class="fa fa-caret-left"></i></button>
                        <span id="pageInfo" class="text-bold"></span>
                        <button class="btn btn-default" id="nextBtn"><i class="fa fa-caret-right"></i></button>
                        <button class="btn btn-success" id="testBtn"><i class="fa fa-check"></i> Complete</button>
<#--                        <button data-dismiss="modal" class="btn btn-default" type="button"><i class="fa fa-times"></i></button>-->
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    var elapsedTime = 0;
    var time = {h:0,m:0,s:0,total:0};
    var intervalId;
    var total = 0;
    var current = 1;
    var percent = 0;
    var pageNo = 1;
    $(document).ready(function() {
        var modal;
        var documentId;
        $('#training-modal').on('show.bs.modal', function (event) {
            current = 1;
            time.h = 0;
            time.m = 0;
            time.s = 0;
            percent = 0;

            var button = $(event.relatedTarget); // Button that triggered the modal
            documentId = button.data('id'); // Extract info from data-* attributes
            $("#docVersionId").val(documentId);
            $("#trainingPeriodId").val(button.data('pid'));
            if(button.data('time')) {
                time.total = parseInt(button.data('time'));

                time.s = time.total % 60;
                if(time.total >= 60) {
                    time.m = Math.round(time.total / 60);
                }
                if(time.total >= 3600) {
                    time.h = Math.round(time.total / 3600);
                }
            }

            if(button.data('percent')) {
                percent = parseInt(button.data('percent'));
            }
            if(button.data('logid')) {
                $("#trainingLogId").val(button.data('logid'));
            }
            total = parseInt(button.data('total')); // Extract info from data-* attributes
            if(button.data('page')) {
                if(confirm('진행중인 교육 이력이 있습니다. 이어서 진행 하시겠습니까?')) {
                    current = parseInt(button.data('page'));
                }
            }
            modal = $(this);
            modal.find('#image-view').html("<img src='/sop/CONFIDENTIAL/viewer/" + documentId + "?page="+current+"' class='img-responsive'/>");
            $("#sopInfo").text(button.data('title'));

            intervalId = setInterval(trainingTime, 1000);
            progress();
            updatePageInfo();
            changeButtonStatus();
        });

        $('#training-modal').on('hide.bs.modal', function(e) {
            if(intervalId) {
                if (!confirm('교육을 종료 하시겠습니까?')) {
                    e.preventDefault();
                } else {
                    // alert('교육시간 업데이트! 교육시간 : ' + time.total + "초");
                    clearInterval(intervalId);
                    intervalId = null;
                    $("#trainingLogForm").submit();
                }
            }
        });

        $("#nextBtn").click(function() {
            if(total > current) {
                current ++;
                modal.find('#image-view').html("<img src='/sop/CONFIDENTIAL/viewer/" + documentId + "?page="+current+"' class='img-responsive'/>");
                progress();

                updatePageInfo();
                changeButtonStatus();
            }
        });
        $("#prevBtn").click(function() {
            if(current > 1) {
                current --;
                modal.find('#image-view').html("<img src='/sop/CONFIDENTIAL/viewer/" + documentId + "?page="+current+"' class='img-responsive'/>");

                updatePageInfo();
                changeButtonStatus();
            }
        });

        $("#testBtn").click(function() {
            $(this).attr("disabled", "disabled");
            if(intervalId) {
                clearInterval(intervalId);
                intervalId = null;
            }

            $("#trainingLogForm").submit();
        });

        $(document).on("mousemove", "#image-view img", function(e) {
            var width = $(this).width();
            var half = width / 2;
            var offsetLeft = $(this).offset().left;
            var posX = e.clientX - offsetLeft;

            if(posX < half) {
                $(this).css('cursor', 'zoom-out');
            } else {
                $(this).css('cursor', 'zoom-in');
            }
        });
        $(document).on("click", "#image-view img", function(e) {
            var width = $(this).width();
            var half = width / 2;
            var offsetLeft = $(this).offset().left;
            var posX = e.clientX - offsetLeft;

            if(posX < half) {
                $("#prevBtn").trigger("click");
            } else {
                $("#nextBtn").trigger("click");
            }
        });

    });

    function trainingTime() {
        time.s ++;
        time.total ++;
        if(time.s >= 60) {
            time.m ++;
            time.s = 0;
        }

        if(time.m >= 60) {
            time.h ++;
            time.m = 0;
        }

        var viewTime = "";
        if(time.h > 0) {
            viewTime = time.h + "시간 ";
        }

        if(time.h != 0 || time.m > 0) {
            viewTime += time.m + "분 ";
        }
        viewTime += time.s + "초";

        $("#trainingTime").val(time.total);
        $("#timer").text(viewTime);
    }

    function progress() {
        var per = (current / total) * 100;
        if(percent < per) {
            percent = Math.round(per);
        }
        $("#progressPercent").val(percent);
        // $("#progressPercentDiv").text(percent + "%");
        $("#progressDiv > .progress-bar").css({"width":(percent+"%")}).text(percent+"%");

        if(per == 100) {
            $("#testBtn").show();
        } else {
            $("#testBtn").hide();
        }
    }

    function updatePageInfo() {
        if(current > pageNo) {
            pageNo = current;
        }
        $("#lastPageNo").val(pageNo);
        $("#pageInfo").text(current + "/"+total);
    }

    function changeButtonStatus() {
        if(total > current) {
            $("#nextBtn").removeAttr("disabled");
        } else {
            $("#nextBtn").attr("disabled", "disabled");
        }

        if(current > 1) {
            $("#prevBtn").removeAttr("disabled");
        } else {
            $("#prevBtn").attr("disabled", "disabled");
        }
    }
</script>
<div class="modal fade" id="test-modal" role="dialog" tabindex="-1" aria-labelledby="test-modal" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <form action="${requirement.name()}-training/test" method="post" id="testForm">
        <div class="modal-content">

            <!--Modal header-->
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><i class="pci-cross pci-circle"></i></button>
                <h4 class="modal-title"></h4>
            </div>

            <!--Modal body-->
            <div class="modal-body">
            </div>

            <!--Modal footer-->
            <div class="modal-footer">
                <div class="row">
                    <div class="col-xs-6 text-left">
                    </div>
                    <div class="col-xs-6">
                        <button data-dismiss="modal" class="btn btn-default" type="button"><i class="fa fa-times"></i> Cancel</button>
                        <button class="btn btn-info" type="submit"><i class="fa fa-check"></i> Submit</button>
                    </div>
                </div>
            </div>
        </div>
        </form>
    </div>
</div>

<script>
    $(document).ready(function() {
        $('#test-modal').on('show.bs.modal', function (event) {
            var button = $(event.relatedTarget); // Button that triggered the modal
            var documentId = button.data('id'); // Extract info from data-* attributes
            var title = button.data('title'); // Extract info from data-* attributes
            var modal = $(this);
            modal.find('.modal-body').html("Loading...");
            modal.find('.modal-title').html(title);
            $.ajax({
                url:'/ajax/training/sop/${requirement.name()}-training/test',
                data:{docVerId:documentId,r:Math.random()},
                success:function(responseText) {
                    // console.log(res);
                    modal.find('.modal-body').html(responseText);
                    setTimeout(function() {
                        $("#testTrainingLogId").val(button.data('logid'))
                    }, 250);
                }
            });
        });

        $("#testForm").submit(function() {
            return testValidate();
        });
    });

    function testValidate() {
        for(var i = 0; i < 5; i ++) {
            var checked = $("#answerDiv"+i+" input[type='checkbox']:checked").length;
            if(checked == 0) {
                $.niftyNoty({
                    type: 'danger',
                    container: 'floating',
                    // html: alert_content[alert_layout].type,
                    message:(i + 1) + '번 문제에 답을 선택해 주세요.',
                    closeBtn: true,
                    floating: {
                        position: 'top-right',
                        animationIn: 'fadeInUp',
                        animationOut: 'fadeOut'
                    },
                    focus: true,
                    timer: 5000
                });
                return false;
            }
        }

        return true;
    }
</script>
</body>
</html>