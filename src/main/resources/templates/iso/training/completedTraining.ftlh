<#import "/common/pagination.ftl" as pagination />
<#setting locale="en_US">
<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<html>
<head>
    <title> ISO MS > Training > My Training > Completed</title>
    <script>
        $(document).ready(function () {
            // $(document).on("click", "button[name='retrainingBtn']", function() {
            //   if(confirm('교육을 다시 진행 하시겠습니까?')) {
            //       $("#tid").val($(this).data("id"));
            //
            //       $("#retrainingForm").submit();
            //   }
            // });

            $("#cert-modal").on("show.bs.modal", function (event) {
                let btn = $(event.relatedTarget);
                let certId = btn.data("cert");
                $("#certId").val(certId);
                $("#certHtml").html('<div class="sk-three-bounce" style="width: 100%">' +
                '<div class="sk-child sk-bounce1"></div>' +
                '<div class="sk-child sk-bounce2"></div>' +
                '<div class="sk-child sk-bounce3"></div><p>Loading...</p></div>');

                $.ajax({
                    url: '/ajax/training/iso/certification/' + certId,
                    method: 'post',
                    data: {"_method": "put", r: Math.random()},
                    success: function (certHtml) {

                        setTimeout(function (){
                            $("#certHtml").html(certHtml);
                        }, 1000);
                    }
                });

                console.log("Cert");
            });

            $("#downloadBtn").click(function (){
                $("#downloadForm").submit();
            })
        });
    </script>
</head>
<body>

<form id="downloadForm" action="/training/iso/mytraining/completed/downloadCertFile" method="post">
    <input type="hidden" id="certId" name="isoCertId"/>
</form>


<!--Data Table-->
<!--===================================================-->
<#--    <@pagination.bind trainingLog/>-->
<#--    <div class="panel-body">-->
<div class="tab-base">
    <ul class="nav nav-tabs">
        <li>
            <a href="/training/iso/mytraining">Ongoing</a>
        </li>
        <li class="active">
            <a href="#">Completed</a>
        </li>
    </ul>
    <div class="tab-content">
        <#--                <div class="table-responsive">-->
        <#--                    <table class="table table-bordered table-hover">-->
        <#--                        <thead>-->
        <#--                        <tr class="bg-gray-light">-->
        <#--                            <th class="text-center" style="width:70px;">No.</th>-->
        <#--                            <th class="text-center">Completion Date<br/><span class="text-muted text-sm"><em>(DD-MMM-YYYY)</em></span> </th>-->
        <#--                            <th>Description of Training Course</th>-->
        <#--                            <th class="text-center">Training<br/>Hr.</th>-->
        <#--                            <th class="text-center">Organization<br/><span class="text-muted text-sm ital"><em>(Academy, Self-training, etc.)</em></span> </th>-->
        <#--                            <th class="text-center">#Actions</th>-->
        <#--                        </tr>-->
        <#--                        </thead>-->
        <#--                        <tbody>-->
        <#--                        <#if trainingLog.content?has_content>-->
        <#--                            <#list trainingLog.content as t>-->
        <#--                                <tr>-->
        <#--                                    <td class="text-center">${(trainingLog.totalElements - (trainingLog.size * trainingLog.number)) - t?index}</td>-->
        <#--                                    <td class="text-center"><#if t.completeDate?has_content>${t.completeDate?string('dd-MMM-yyyy')}</#if></td>-->
        <#--                                    <td>${t.trainingCourse}</td>-->
        <#--                                    <td class="text-center">${t.hour}</td>-->
        <#--                                    <td class="text-center">${t.organization}</td>-->
        <#--                                    <td class="text-center">-->
        <#--                                        <#if t.type == 'SELF'>-->
        <#--                                        <button class="btn btn-info btn-xs" name="retrainingBtn" data-id="${t.id}">Re-training</button>-->
        <#--                                        </#if>-->
        <#--                                    </td>-->
        <#--                                </tr>-->
        <#--                            </#list>-->
        <#--                        <#else>-->
        <#--                            <tr>-->
        <#--                                <td colspan="6" class="text-center">교육 완료 이럭이 존재하지 않습니다.</td>-->
        <#--                            </tr>-->
        <#--                        </#if>-->
        <#--                        </tbody>-->
        <#--                    </table>-->
        <#--                </div>-->
        <#--                <@pagination.default/>-->


        <link href="/static/plugins/bootstrap-table/bootstrap-table.min.css" rel="stylesheet">
        <script src="/static/plugins/bootstrap-table/bootstrap-table.min.js"></script>
        <script>
            function tableActions(value, row, index) {
                    return [
                        '<button type="button" id="certBtn" name="certificationBtn" data-target="#cert-modal" data-toggle="modal" class="btn btn-info btn-xs" ',
                        '" data-cert="' + row.certId + '">',
                        'Certificate Output',
                        '</button>'
                    ].join('');
            }
        </script>
        <table data-toggle="table"
               data-url="/ajax/training/iso/mytraining/completed"
                <#--                       data-show-columns="true"-->
                <#--                       data-toolbar="#toolbar"-->
               data-page-list="[5, 10, 20, 50]"
               data-page-size="20"
               data-pagination="true"
               data-show-pagination-switch="false"
               data-search="true"
               data-search-on-enter-key="true"
               id="codingTbl">
            <thead>
            <tr class="bg-gray-light">
                <th data-sortable="true" data-field="index">No.</th>
                <th data-sortable="true" data-field="completionDate">Completion Date<br><span
                            class="text-muted text-sm"><em>(DD-MMM-YYYY)</em></span></th>
                <th data-sortable="true" data-field="course">Description of Training Course</th>
                <th data-sortable="true" data-field="hour">Training Hr.</th>
                <th data-sortable="true" data-field="organization">Organization<br/><span
                            class="text-muted text-sm"><em>(Academy, Self-training, etc.)</em></span></th>
                <th data-formatter="tableActions">#Actions</th>
            </tr>
            </thead>
        </table>
    </div>

</div>

<!--===================================================-->
<!--End Data Table-->

<div class="modal fade" id="cert-modal" role="dialog" tabindex="-1" aria-labelledby="training-modal"
     aria-hidden="true">
    <div class="modal-dialog" style="width:auto">
        <div class="modal-content">
            <!--Modal header-->
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><i class="pci-cross pci-circle"></i></button>
                <h4 class="modal-title">ISO Self Training : <span id="isoTitle"></span></h4>
            </div>

            <!--Modal body-->
            <div class="modal-body">
                <div id="certHtml"></div>
            </div>

            <!--Modal footer-->
            <div class="modal-footer">
                <button class="btn btn-danger" id="downloadBtn"><i class="fa fa-file"></i> Download</button>
                <button class="btn btn-default" data-dismiss="modal"><i class="fa fa-close"></i> Close</button>
            </div>
        </div>
    </div>
</div>
</body>
</html>
