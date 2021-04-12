<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<html>
<head>
    <title>ISO-MS :: ${viewIso.title}</title>
    <!--Bootbox Modals [ OPTIONAL ]-->
    <script src="/static/plugins/bootbox/bootbox.min.js"></script>

    <!-- Data Table -->
    <link href="/static/plugins/datatables/media/css/jquery.dataTables.min.css" rel="stylesheet">
    <link href="/static/plugins/datatables/media/css/dataTables.bootstrap4.min.css" rel="stylesheet">
    <!-- Data Table -->
    <script src="/static/plugins/datatables/media/js/jquery.dataTables.min.js"></script>
    <script src="/static/plugins/datatables/media/js/dataTables.bootstrap4.min.js"></script>

    <style>
        .modal-fullsize {
            width: 80%;
            height: 800px;
            margin: 3px;
            padding: 0;
        }

        #iso-html {
            height: 700px;
        }
    </style>

</head>
<body>


<script>
    // function preview_print(){
    //     var OLECMDID = 7;
    //     var PROMPT = 1;
    //     var WebBrowser = '<OBJECT ID="WebBrowser1" WIDTH=0 HEIGHT=0 CLASSID="CLSID:8856F961-340A-11D0-A96B-00C04FD705A2"></OBJECT>';
    //
    //
    //     document.body.insertAdjacentHTML('beforeEnd', WebBrowser);
    //     WebBrowser1.ExecWB( OLECMDID, PROMPT);
    // }

    function fnPrint() {
        // var agent = navigator.userAgent.toLowerCase();
        // if(agent.indexOf("trident") > 0 || agent.indexOf("msie") > 0 ){
        //     preview_print();
        // } else {
        window.print();
        // }
    }

    $(document).ready(function () {
        $('#removeBtn').on('click', function () {
            bootbox.confirm("정말로 삭제 하시겠습니까?", function (result) {
                if (result) {
                    $("#removeForm").submit();
                } else {
                    // $.niftyNoty({
                    //     type: 'danger',
                    //     icon: 'pli-cross icon-2x',
                    //     message: 'User declined dialog.',
                    //     container: 'floating',
                    //     timer: 5000
                    // });
                }

            });
        });

        $("#sendEmailBtn").click(function () {
            if (confirm('알림 메일을 전송 하시겠습니까?')) {
                var isoId = $(this).data("id");
                $.ajax({
                    url: '/ajax/iso-14155/' + isoId + '/send',
                    data: {r: Math.random()},
                    success: function (r) {
                        // alert(r);
                        // $("#sendEmailBtn").hide();
                        alert('이메일 전송 요청이 완료 되었습니다.');
                    }
                })
            }
        });

        $('#attendUserList').dataTable({
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
            ]
        });
    });


</script>
<form id="removeForm" method="post">
    <input type="hidden" name="_method" value="delete">
</form>
<div class="panel">
    <div class="panel-body">
        <div class="fixed-fluid">

            <div class="fluid">

                <!-- VIEW MESSAGE -->
                <!--===================================================-->

                <div class="mar-btm pad-btm bord-btm">
                    <h1 class="page-header text-overflow">
                        <span class="label label-${viewIso.training?then('info','mint')}">${viewIso.training?then('Training','Board')}</span>
                        ${viewIso.title}
                    </h1>
                </div>

                <div class="row">
                    <div class="col-sm-7 toolbar-left">
                        <!--Sender Information-->
                        <div class="media">
                            <div class="media-body text-left">
                                <div class="text-bold"><i class="pli-user"></i> ${viewIso.createdBy?default('System')}
                                </div>
                                <small class="text-muted"><i class="pli-pencil"></i> ${viewIso.createdDate}</small>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-5 toolbar-right">

                        <!--Details Information-->
                        <#--                        <p class="mar-no"><small class="text-muted">${notice.createdDate}</small></p>-->
                        <#--                        <a href="#">-->
                        <#--                            <strong>Holiday.zip</strong>-->
                        <#--                            <i class="psi-paperclip icon-lg icon-fw"></i>-->
                        <#--                        </a>-->
                    </div>
                </div>

                <!-- Training ISO 인 경우, -->
                <#if viewIso.training>
                    <table class="table table-bordered">
                        <thead>
                        <tr>
                            <th class="text-center">교육 참석</th>
                            <th class="text-center">참석자</th>
                            <th class="text-center">교육 기간</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td class="text-center">
                                <span class="label label-${viewIso.trainingAll?then('info', 'purple')}">${viewIso.trainingAll?then('ALL', 'PART')}</span>
                            </td>
                            <td class="text-center">
                                <#if userPageList?has_content>
                                    <button type="button" data-target="#attend-modal" data-toggle="modal" class="btn btn-purple btn-xs">${userPageList.content?size}명</button>
                                <#else>
                                    -
                                </#if>
                            </td>
                            <td class="text-center text-info">${viewIso.trainingDate}</td>
                        </tr>
                        </tbody>
                    </table>
                </#if>
                <div class="row pad-top hidden-print">
<#--                    <div class="col-sm-7 toolbar-left">-->
<#--                        <!--Mail toolbar&ndash;&gt;-->
<#--                        <button class="btn btn-default" type="button" onclick="fnPrint();"><i-->
<#--                                    class="pli-printer icon-lg"></i></button>-->
<#--                        &lt;#&ndash;                        <div class="btn-group btn-group">&ndash;&gt;-->
<#--                        <@security.authorize access="hasAnyAuthority('ADMIN')">-->
<#--                            <button class="btn btn-default" id="sendEmailBtn" data-id="${viewIso.id}"><i-->
<#--                                        class="fa fa-send-o"></i> 메일 전송-->
<#--                            </button>-->
<#--                        </@security.authorize>-->
<#--                        &lt;#&ndash;                        </div>&ndash;&gt;-->
<#--                    </div>-->
                    <div class="col-sm-offset-7 col-sm-5 toolbar-right">
                        <!--Reply & forward buttons-->
                        <@security.authorize access="hasAnyAuthority('ADMIN')">
                            <a class="btn btn-default" href="javascript:goToURL('/iso-14155/${viewIso.id}/edit');"><i
                                        class="pli-pencil"></i> 수정</a>
                            <button class="btn btn-default" id="removeBtn"><i class="pli-trash icon-lg"></i> 삭제</button>
                        </@security.authorize>
                        <div class="btn-group btn-group">
                            <a class="btn btn-default" href="javascript:goToURL('/iso-14155/${viewIso.training?then('training', 'board')}');">
                                <i class="psi-list-view"></i> 목록
                            </a>
<#--                            <#if categoryCode?has_content>-->
<#--                                <a class="btn btn-default" href="javascript:goToURL('/iso-14155/${viewIso.training?then('training', 'board')}?categoryCode=${categoryCode}');">-->
<#--                                    <i class="psi-list-view"></i> 목록-->
<#--                                </a>-->
<#--                            <#else>-->
<#--                                <a class="btn btn-default" href="javascript:goToURL('/iso-14155/${viewIso.training?then('training', 'board')}');">-->
<#--                                    <i class="psi-list-view"></i> 목록-->
<#--                                </a>-->
<#--                            </#if>-->
                        </div>
                    </div>
                </div>

                <!--Message-->
                <!--===================================================-->
                <div class="mail-message">
                    ${viewIso.content?no_esc}
                </div>
                <!--===================================================-->
                <!--End Message-->

                <!-- Attach Files-->
                <!--===================================================-->
                <#if viewIso.attachFiles?has_content>
                    <div class="pad-ver">
                        <p class="text-main text-bold box-inline"><i class="psi-paperclip icon-fw"></i> Attachments
                            <span>(${viewIso.attachFiles?size})</span></p>
                        <#--                    <a href="#" class="btn-link">Download all</a> | <a href="#" class="btn-link">View all images</a>-->

                        <ul class="mail-attach-list list-ov">
                            <#list viewIso.attachFiles as attachFile>
                                <li>
                                    <a href="#iso-modal" data-toggle="modal" data-id="${viewIso.id}"
                                       data-file="${attachFile.id}" data-name="${attachFile.originalFileName}"
                                       class="thumbnail">
                                        <#if attachFile.fileType?index_of('image') == 0>
                                            <div class="mail-file-img">
                                                <img class="image-responsive"
                                                     src="/iso-14155/${viewIso.id}/downloadFile/${attachFile.id}"
                                                     alt="image">
                                            </div>
                                        <#else>
                                            <div class="mail-file-icon">
                                                <i class="pli-file-download"></i>
                                            </div>
                                        </#if>
                                        <div class="caption">
                                            <p class="text-main mar-no"
                                               style="overflow-x: hidden;">${attachFile.originalFileName}</p>
                                            <small class="text-muted">Added: ${attachFile.createdDate}</small>
                                        </div>
                                    </a>
                                </li>
                            </#list>
                        </ul>
                    </div>
                </#if>
                <!--===================================================-->
                <!-- End Attach Files-->
            </div>

            <!-- ISO Viewer -->
            <div class="modal fade" id="iso-modal" role="dialog" tabindex="-1" aria-labelledby="iso-modal"
                 aria-hidden="true">
                <div class="modal-dialog modal-fullsize">
                    <div class="modal-content">
                        <!--Modal header-->
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal"><i
                                        class="pci-cross pci-circle"></i></button>
                            <h4 class="modal-title">ISO 14155</h4>
                        </div>

                        <!--Modal body-->
                        <div class="modal-body">
                            <div class="table-responsive">
                                <div id="iso-html" style="">내용을 불러오는 중입니다...</div>
                            </div>
                        </div>

                        <!--Modal footer-->
                        <div class="modal-footer">
                            <button data-dismiss="modal" class="btn btn-default" type="button"><i
                                        class="fa fa-times"></i> Close
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <#-- PDF Object -->
            <script src='/static/js/pdfjs/control/pdfobject.js'></script>
            <!-- ISO viewer end -->
            <script>
                $('#iso-modal').on('show.bs.modal', function (event) {
                    $("#iso-html").html('<string>내용을 불러오는 중입니다...</string>');
                    var button = $(event.relatedTarget); // Button that triggered the modal
                    var isoId = button.data('id');
                    var fileId = button.data('file');
                    var isoName = button.data('name');

                    isoName = isoName.substr(0, isoName.indexOf(".pdf"));
                    $(".modal-title").html(isoName);

                    var options = {
                        height: "800",
                        page: '1',
                        pdfOpenParams: {
                            view: 'FitV',
                            pagemode: 'thumbs',
                            search: 'lorem ipsum'
                        },
                        forcePDFJS: true, // 강제로 PDF Viewer를 실행시키는 옵션
                        PDFJS_URL: "/static/js/pdfjs/web/viewer.html" //PDF.js의 PDF viewer를 viewer로 지정.
                    };

                    //제일 처음 열리는 pdf파일 경로 설정.
                    PDFObject.embed("/iso-14155/" + isoId + "/downloadFile/" + fileId, "#iso-html", options);
                });
            </script>

            <!-- ISO Viewer -->
            <div class="modal fade" id="attend-modal" role="dialog" tabindex="-1" aria-labelledby="attend-modal"
                 aria-hidden="true">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <!--Modal header-->
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal"><i
                                        class="pci-cross pci-circle"></i></button>
                            <h4 class="modal-title">Training 참석자 명단</h4>
                        </div>

                        <!--Modal body-->
                        <div class="modal-body">
                            <div class="table-responsive">
                                <table id="attendUserList" class="table table-striped">
                                    <thead>
                                    <tr>
                                        <th class="text-center">No.</th>
                                        <th class="text-center">부서</th>
                                        <th class="text-center">팀</th>
                                        <th class="text-center">이름</th>
                                        <th class="text-center">배정 일자</th>
                                    </tr>
                                    </thead>
                                    <tbody>
<#--                                    TODO -->
                                    <#if userPageList?has_content>
                                        <#list userPageList.content as u>
                                            <tr>
                                                <td class="text-center">${(userPageList.totalElements - (userPageList.size * userPageList.number)) - u?index}</td>
                                                <td class="text-center">${u.deptName}</td>
                                                <td class="text-center">${u.teamName}</td>
                                                <td class="text-center">${u.name}</td>
                                                <td class="text-center">${u.createdDate?string('yyyy-MM-dd')}</td>
                                            </tr>
                                        </#list>
                                    </#if>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!--Modal footer-->
                        <div class="modal-footer">
                            <button data-dismiss="modal" class="btn btn-default" type="button"><i
                                        class="fa fa-times"></i> Close
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>