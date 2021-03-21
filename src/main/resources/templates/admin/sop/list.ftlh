<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#import "/common/pagination.ftl" as pagination />
<html>
<head>
    <title>ISO Management System > 관리자 > SOP Management</title>
    <script src="/static/plugins/bootbox/bootbox.min.js"></script>
    <link href="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.css" rel="stylesheet">
    <script src="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.js"></script>

    <script>
        $(document).ready(function() {
            $('button[name=retirement]').on('click', function () {
                var docVerId = $(this).data("id");

                bootbox.dialog({
                    title: '${type.name()} Retirement',
                    message: "<div>"
                        + "<p>Superseded 상태로 변경 하시겠습니까?<br><em>Superseded 기간 미적용 시 곧바로 반영됩니다.</em></p>"
                        + "<table class='table table-striped'>"
                        + "<tr>"
                        + "<td style='width: 160px; vertical-align: middle;'><span class='label label-primary label-md'>Superseded Date</span></td>"
                        + "<td><input type='text' class='form-control' id='checkDate' /></td>"
                        + "</tr>"
                        + "</table>"
                        + "</div>"
                        + "<script>$('#checkDate').datepicker({format:'yyyy-mm-dd', autoclose:true, clearBtn:true, todayHighlight: true, startDate:'+2D'});<\/script>",
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
                                let date = $("#checkDate").val();

                                if (date == "") {
                                    $("#statusChangeForm").attr("action", '/admin/${type.name()}/management/${status.name()?lower_case}/' + docVerId + '/retirement');
                                    $("#statusChangeForm").submit();
                                } else {
                                    $("#statusChangeForm").attr("action", '/admin/${type.name()}/management/${status.name()?lower_case}/' + docVerId + '/retirement');
                                    $("#retirementDate").val(date);
                                    $("#statusChangeForm").submit();
                                }
                            }
                        }
                    }

                <#--bootbox.confirm("", function (result) {-->
                <#--    if (result) {-->
                <#--        $("#statusChangeForm").attr("action", '/admin/${type.name()}/management/${status.name()?lower_case}/'+docVerId+'/retirement');-->
                <#--        $("#statusChangeForm").submit();-->
                <#--    } else {-->

                <#--    }-->
                });
            });


            $('button[name=approved]').on('click', function () {
                var docVerId = $(this).data("id");
                bootbox.confirm("Approved 상태로 변경 하시겠습니까?", function (result) {
                    if (result) {
                        $("#statusChangeForm").attr("action", '/admin/${type.name()}/management/${status.name()?lower_case}/'+docVerId+'/approved');
                        $("#statusChangeForm").submit();
                    } else {
                    }
                });
            });
            $('button[name=remove]').on('click', function () {
                var docVerId = $(this).data("id");
                var docTitle = $(this).data("title");
                bootbox.confirm("["+docTitle+"] 삭제 하시겠습니까?", function (result) {
                    if (result) {
                        $("#removeForm").attr("action", '/admin/${type.name()}/management/${status.name()?lower_case}/'+docVerId+'/remove');
                        $("#removeForm").submit();
                    } else {
                    }
                });
            });
        });
    </script>
</head>
<body>
<form id="statusChangeForm" method="post">
    <input type="hidden" name="_method" value="PUT"/>
    <input type="hidden" name="retirementDate" id="retirementDate"/>
</form>
<form id="removeForm" method="post">
    <input type="hidden" name="_method" value="DELETE"/>
</form>

<div class="panel">
<#--    <div class="panel-heading">-->
<#--        <h3 class="panel-title">${status.label} ${type.name()}</h3>-->
<#--    </div>-->

    <!--Data Table-->
    <!--===================================================-->
    <div class="panel-body">
        <div class="pad-btm form-inline">
            <div class="row">
                <div class="col-sm-6 table-toolbar-left">

                    <#if status == 'EFFECTIVE' || status == 'SUPERSEDED' || status == 'APPROVED' || status == 'DEVELOPMENT'>
                        <a class="btn btn-primary" href="javascript:goToURL('/admin/${type.name()}/management/${status.name()?lower_case}/new')"><i class="pli-pencil"></i> ${status.label} ${type.name()} 등록</a>
                    </#if>
<#--                    <#if status == 'EFFECTIVE'>-->
<#--                        <a href="/common/download/${type.name()}/index" class="btn btn-mint"><i class="fa fa-download"></i> ${type.name()} Index</a>-->
<#--                    </#if>-->
                </div>
                <div class="col-sm-6 table-toolbar-right">
                    <form>
                    <div class="form-group">
                        <input id="searchDocId" name="docId" type="text" placeholder="Document Id" <#if searchDocId?has_content>value="${searchDocId}"</#if> class="form-control" autocomplete="off">
                    </div>
                    <div class="btn-group">
                        <button class="btn btn-default" type="submit"><i class="fa fa-search fa-lg"></i></button>
                    </div>
                    </form>
                </div>
            </div>
        </div>
        <@pagination.bind documentList/>
<#--        <div class="table-responsive">-->
            <table class="table table-striped">
                <thead>
                <tr>
                    <th class="text-center" style="width:70px;">No.</th>
                    <th class="text-center"><i class="fa fa-file-pdf-o"></i></th>
                    <th>
                        <#if type == 'SOP'>
                        <@pagination.sortField "category" "document.category.shortName"/>
                        <#else>
                        <@pagination.sortField "SOP" "document.sop.docId"/>
                        </#if>
                    </th>
                    <th style="width:140px">
                        <@pagination.sortField "Doc Id" "document.docId"/></th>
                    <th>Title</th>
                    <th style="width:100px" class="text-center">Version</th>
                    <th style="width:120px;" class="text-center">Effective Date</th>
<#--                    <th class="text-center" style="width:160px;">등록일</th>-->
<#--                    <th class="text-center" style="width:140px">작성자</th>-->
                    <th class="text-center">#actions</th>
                    <#--                    <th class="text-center" style="width:120px">#actions</th>-->
                </tr>
                </thead>
                <tbody>
                <#if documentList.content?has_content>
                    <#list documentList.content as documentVer>
                        <tr>
                            <td class="text-center">${(documentList.totalElements - (documentList.size * documentList.number)) - documentVer?index}</td>
                            <td class="text-center">
                                <#if documentVer.fileName?has_content>
                                <i class="fa fa-file-pdf-o"></i>
                                </#if>
                            </td>
                            <td>
                            <#if type == 'SOP'>
                                ${documentVer.document.category.name}
                            <#else>
                                ${documentVer.document.sop.docId}
                            </#if>
                            </td>
                            <td>${documentVer.document.docId}</td>
                            <td>${documentVer.document.title}</td>
                            <td class="text-center">${documentVer.version?default('<span class="label label-warning">N/A</span>')?no_esc}</td>
                            <td class="text-center"><#if documentVer.effectiveDate?has_content>${documentVer.effectiveDate?string('yyyy-MM-dd')}<#else><span class="label label-warning">N/A</span></#if></td>
<#--                            <td class="text-center"><span class="text-muted">${documentVer.document.createdDate?string('yyyy-MM-dd HH:mm:ss')}</span></td>-->
<#--                            <td class="text-center">${documentVer.document.createdBy?default('-')}</td>-->
                            <td class="text-center">
<#--                                <div class="btn-group">-->
                                <#if type == 'SOP'><!--quiz-->
                                    <#if status == 'DEVELOPMENT' || status == 'REVISION' || status == 'APPROVED' || status == 'EFFECTIVE'>
                                        <!--===================================================-->
                                        <#if !documentVer.retirementDate?has_content>
                                            <div class="btn-group btn-group-xs dropdown">
                                                <button class="btn btn-${(documentVer.quiz?has_content)?then('info', 'default')} btn-active-pink dropdown-toggle dropdown-toggle-icon" data-toggle="dropdown" type="button">
                                                    Quiz <i class="dropdown-caret"></i>
                                                </button>
                                                <ul class="dropdown-menu">
                                                    <li><a href="/admin/${type.name()}/management/${status.name()?lower_case}/${documentVer.id}/quiz"><i class="fa fa-pencil"></i> Edit</a></li>
                                                    <li><a href="/admin/${type.name()}/management/${status.name()?lower_case}/${documentVer.id}/quiz/upload"><i class="fa fa-upload"></i> Template Upload</a></li>
                                                    <#if documentVer.quiz?has_content>
                                                    <li class="divider"></li>
                                                    <li><a href="/admin/${type.name()}/management/${status.name()?lower_case}/${documentVer.id}/quiz/test"><i class="fa fa-check-square-o"></i> Test</a></li>
                                                    </#if>
                                                </ul>
                                            </div>
                                        </#if>
                                        <!--===================================================-->
<#--                                    <a class="btn btn-xs btn-${(documentVer.quiz?has_content)?then('info', 'default')}" href="/admin/${type.name()}/management/${status.name()?lower_case}/${documentVer.id}/quiz"><i class="fa fa-${(documentVer.quiz?has_content)?then('check', 'question-circle-o')}"> Quiz</i></a>-->
                                    </#if>
                                </#if>
                                <#if status == 'EFFECTIVE'>
                                    <#if !documentVer.retirementDate?has_content>
                                        <a class="btn btn-xs btn-purple" href="/admin/${type.name()}/management/${status.name()?lower_case}/${documentVer.id}/revision"><i class="fa fa-plus"> Revision</i></a>
                                        <button name="retirement" type="button" class="btn btn-xs btn-danger" data-id="${documentVer.id}"><i class="fa fa-trash"> Retirement</i></button>
                                    <#else>
                                        <span class="label label-dark add-popover"
                                                data-toggle="popover" data-container="body" data-placement="bottom"
                                                data-trigger="hover" data-html="true"
                                                data-content="<div>Retirement 예정일 : ${documentVer.retirementDate?string('yyyy-MM-dd')}</div>"
                                                style="max-width: 160px; cursor: pointer;">
                                            <i class="fa fa-trash-o"></i>
                                        </span>&nbsp;
                                    </#if>
                                <#elseif status == 'DEVELOPMENT' || status == 'REVISION'>
                                    <button name="approved" type="button" class="btn btn-xs btn-success" data-id="${documentVer.id}"><i class="fa fa-check"> Approved</i></button>
                                </#if>

                                <#if status != 'SUPERSEDED'>
                                <a class="btn btn-xs btn-mint" href="/admin/${type.name()}/management/${status.name()?lower_case}/${documentVer.id}/edit"><i class="fa fa-pencil"> Edit</i></a>
                                </#if>
                                <#if status == 'REVISION' || status == 'DEVELOPMENT'>
                                    <button class="btn btn-xs btn-danger" data-title="${documentVer.document.docId} ${documentVer.document.title}" name="remove" type="button" data-id="${documentVer.id}"><i class="fa fa-trash-o"> Remove</i></button>
                                </#if>
<#--                                </div>-->
                            </td>
                            <#--                    <td class="text-center">-->
                            <#--                        <a href="/notice/${notice.id}/edit" class="btn-link">edit</a>-->
                            <#--                    </td>-->
                        </tr>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="${(type == 'SOP')?then(8, 8)}" class="text-center">${status.label} ${type.name()}가 존재하지 않습니다.</td>
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
