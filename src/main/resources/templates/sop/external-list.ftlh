<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<@security.authentication property="principal" var="user"/>
<#setting locale="en_US">

<#function getExtIcon ext>
    <#local returnValue = "fa-file-text-o"/>
    <#if ext?has_content>
        <#if ext?index_of('xls') == 0>
            <#return "fa-file-excel-o text-success"/>
        <#elseif ext?index_of('doc') == 0>
            <#return "fa-file-word-o text-primary"/>
        <#elseif ext?index_of('pdf') == 0>
            <#return "fa-file-pdf-o text-danger"/>
        </#if>
    </#if>
    <#return returnValue/>
</#function>

<#macro sopRfList>
<#--                            <li>-->
<#--                                <div class="file-control">-->
<#--                                </div>-->
<#--                                <div class="file-attach-icon"></div>-->
<#--                                <a href=".." class="file-details">-->
<#--                                    <div class="media-block">-->
<#--                                        <div class="media-left"><i class="psi-folder" style="color:#ffc848;"></i></div>-->
<#--                                        <div class="media-body">-->
<#--                                            <p class="file-name single-line">...</p>-->
<#--                                        </div>-->
<#--                                    </div>-->
<#--                                </a>-->
<#--                            </li>-->
    <#list sopList as doc>
        <#if doc.documentVersion.document.type == 'SOP'>
            <li>
                <div class="file-control">
                </div>
                <div class="file-attach-icon"></div>
                <span  class="file-details">
                    <div class="media-block">
                        <div class="media-right">
                            <a class="btn btn-default" href="#document-modal" data-toggle="modal" data-document-id="${doc.documentVersion.id}" data-total="${(doc.documentVersion.totalPage?has_content)?then(doc.documentVersion.totalPage, 0)}">
                                <i class="fa fa-search-plus"></i>
                            </a>
                        </div>
                        <div class="media-left"><i class="fa fa-file-pdf-o text-danger"></i></div>
                        <div class="media-body">
                            <p class="file-name">${doc.document.docId} v${doc.documentVersion.version?has_content?then(doc.documentVersion.version, ' N/A')}_${doc.document.title}</p>
                            <small>Effective Date : ${doc.documentVersion.effectiveDate?string["dd/MMM/yyyy"]}</small>
                        </div>
                    </div>
                </span>
            </li>
        <#else>
            <#if doc.documentVersion.rfEngFileName?has_content>
                <li>
                    <div class="file-control">
                    </div>
                    <div class="file-attach-icon"></div>
                    <span class="file-details">
                        <div class="media-block">
                            <div class="media-right">
                                <a href="#rf-modal" data-toggle="modal" data-id="${doc.documentVersion.id}" data-lang="eng" class="btn btn-default"><i class="fa fa-search-plus"></i></a>
                            </div>
                            <div class="media-left"><i class="fa ${getExtIcon(doc.documentVersion.ext?default('')?lower_case)}"></i></div>
                            <div class="media-body">
                                <p class="file-name">${doc.documentVersion.rfEngOriginalFileName}</p>
                                <small>${doc.documentVersion.document.docId} v${doc.documentVersion.version}_${doc.documentVersion.document.title} / Effective Date : ${doc.documentVersion.effectiveDate?string["dd/MMM/yyyy"]}</small>
                            </div>
                        </div>
                    </span>
                </li>
            </#if>
            <#if doc.documentVersion.rfKorFileName?has_content>
                <li>
                    <div class="file-control">
                    </div>
                    <div class="file-attach-icon"></div>
                    <span class="file-details">
                        <div class="media-block">
                            <div class="media-right">
                                <#if user.userType == 'USER'>
                                    <a href="/rf/${status.name()?lower_case}/download/${doc.documentVersion.id}?lang=kor" class="btn btn-default"><i class="fa fa-download"></i></a>
                                </#if>
                                <a href="#rf-modal" data-toggle="modal" data-id="${doc.documentVersion.id}" data-lang="kor" class="btn btn-default"><i class="fa fa-search-plus"></i></a>
                            </div>
                            <div class="media-left"><i class="fa ${getExtIcon(doc.documentVersion.ext?default('')?lower_case)}"></i></div>
                            <div class="media-body">
                                <p class="file-name">${doc.documentVersion.rfKorOriginalFileName}</p>
                                <small>${doc.documentVersion.document.docId} v${doc.documentVersion.version}_${doc.documentVersion.document.title} / Effective Date : ${doc.documentVersion.effectiveDate?string["dd/MMM/yyyy"]}</small>
                            </div>
                        </div>
                    </span>
                </li>
            </#if>
        </#if>
    </#list>
</#macro>

<html>
<head>
    <title>SOP > ${status.label} SOP</title>
    <#--    <script src="/static/js/slippry.min.js"></script>-->
    <#--    <link href="/static/css/slippry.css" rel="stylesheet">-->
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
</head>
<body>
<div class="panel panel-bordered panel-<#if status == 'EFFECTIVE'>primary<#elseif status == 'APPROVED'>info<#else>warning</#if>">
    <div class="pad-all file-manager">
        <div class="fixed-fluid">
            <div class="fixed-sm-300 pull-sm-left file-sidebar" style="min-height: 400px">

                <p class="pad-hor mar-top text-main text-bold text-sm text-uppercase">CATEGORY</p>
                <div class="list-group bg-trans pad-btm bord-btm">
                    <#--                            <div class="nano scrollable">-->
                    <#--                                <div class="nano-content">-->
                    <#--                    <a href="#" class="list-group-item">-->
                    <#--                        <i class="pli-folder icon-lg icon-fw"></i> ...-->
                    <#--                    </a>-->

                    <a href="/external/sop/${status.name()?lower_case}" class="list-group-item<#if categoryId?has_content == false> text-bold</#if>">
                        <span class="text-main"><i class="pli-folder-with-document icon-lg icon-fw"></i> ALL</span>
                    </a>
                    <#if CategoryList?has_content>
                        <#list CategoryList as Category>
                            <a href="/external/sop/${status.name()?lower_case}/${Category.id}" class="list-group-item<#if categoryId?default('') == Category.id> text-bold</#if>">
                                <span class="text-main"><i class="pli-folder<#if categoryId?default('') == Category.id>-with-document</#if> icon-lg icon-fw"></i> ${Category.name}</span>
                            </a>
                        </#list>
                    </#if>



                </div>
                <div class="text-xs text-warning text-center">
                    <i class="fa fa-exclamation-circle"></i> 모든 이력은 시스템에 기록 됩니다.
                </div>
                <#--                    </div>-->
                <#--                </div>-->
            </div>
            <div class="fluid file-panel">
                <div class="bord-btm pad-ver">
                    <ol class="breadcrumb">
                        <li>SOP</li>
                        <li><a href="/external/sop/${status.name()?lower_case}">${status.label} SOP</a></li>
                        <#if Category?has_content>
                            <li><a href="/external/sop/${status.name()?lower_case}/${Category.id}">${Category.name}</a></li>
                        </#if>

<#--                        <#if sopId?has_content>-->
<#--                            <#list sopList as sopVer>-->
<#--                                <#if sopVer?is_first>-->
<#--                                    <li class="active">${sopVer.document.docId}_${sopVer.document.title}</li>-->
<#--                                </#if>-->
<#--                            </#list>-->
<#--                        </#if>-->
                    </ol>
                </div>
                <#--                <div class="file-toolbar bord-btm">-->
                <#--                    <div class="btn-file-toolbar">-->
                <#--                        <a class="btn btn-icon add-tooltip" href="#" data-original-title="Home" data-toggle="tooltip"><i class="icon-2x pli-home"></i></a>-->
                <#--                        &lt;#&ndash;                        <a class="btn btn-icon add-tooltip" href="#" data-original-title="Refresh" data-toggle="tooltip"><i class="icon-2x pli-reload-3"></i></a>&ndash;&gt;-->
                <#--                    </div>-->
                <#--                    <div class="btn-file-toolbar">-->
                <#--                        <a class="btn btn-icon add-tooltip" href="#" data-original-title="New Folder" data-toggle="tooltip"><i class="icon-2x pli-folder"></i></a>-->
                <#--                        &lt;#&ndash;                        <a class="btn btn-icon add-tooltip" href="#" data-original-title="New File" data-toggle="tooltip"><i class="icon-2x pli-file-add"></i></a>&ndash;&gt;-->
                <#--                        &lt;#&ndash;                        <a class="btn btn-icon add-tooltip" href="#" data-original-title="Edit File" data-toggle="tooltip"><i class="icon-2x pli-file-edit"></i></a>&ndash;&gt;-->
                <#--                    </div>-->
                <#--                    <div class="btn-file-toolbar pull-right">-->
                <#--                        &lt;#&ndash;                        <a class="btn btn-icon add-tooltip" href="#" data-original-title="Delete" data-toggle="tooltip"><i class="icon-2x pli-recycling"></i></a>&ndash;&gt;-->
                <#--                        <a class="btn btn-icon add-tooltip" href="#" data-original-title="Download" data-toggle="tooltip"><i class="icon-2x pli-download-from-cloud"></i></a>-->
                <#--                    </div>-->
                <#--                </div>-->
                <ul id="sop-list" class="file-list">
                        <!--File list item-->
                    <#if sopId?has_content == false>
                        <#list sopList as sop>
                        <li>
                            <div class="file-control">
                            </div>
                            <div class="file-attach-icon"></div>
                            <a href="/external/sop/${status.name()?lower_case}/${sop.sopDocument.category.id}/${sop.sopDocument.id}" class="file-details">
                                <div class="media-block">
                                    <div class="media-left" style="color:#ffc848"><i class="psi-folder"></i></div>
                                    <div class="media-body">
                                        <p class="file-name single-line">${sop.sopDocument.docId}_${sop.sopDocument.title}</p>
                                    </div>
                                </div>
                            </a>
                        </li>
                        </#list>
                    <#else>
                        <@sopRfList/>
                    </#if>

                </ul>
            </div>
        </div>
    </div>

</div>

<!-- RF Viewer -->
<div class="modal fade" id="rf-modal" role="dialog" tabindex="-1" aria-labelledby="rf-modal" aria-hidden="true">
    <div class="modal-dialog modal-fullsize">
        <div class="modal-content">

            <!--Modal header-->
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><i class="pci-cross pci-circle"></i></button>
                <h4 class="modal-title">${formName} Viewer</h4>
            </div>



            <!--Modal body-->
            <div class="modal-body">
                <div class="table-responsive">
                    <div id="rf-html">문서를 불러오는 중입니다...</div>
                </div>
            </div>

            <!--Modal footer-->
            <div class="modal-footer">
                <button data-dismiss="modal" class="btn btn-default" type="button"><i class="fa fa-times"></i> Close</button>
            </div>
        </div>
    </div>
</div>
<!-- RF viewer end -->


<div class="modal fade" id="document-modal" role="dialog" tabindex="-1" aria-labelledby="document-modal" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">

            <!--Modal header-->
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><i class="pci-cross pci-circle"></i></button>
                <h4 class="modal-title">SOP Viewer</h4>
            </div>

            <!--Modal body-->
            <div class="modal-body">
                <div id="image-view"></div>
            </div>

            <!--Modal footer-->
            <div class="modal-footer">
                <#--                <button data-dismiss="modal" class="btn btn-default" type="button">Close</button>-->
                <#--                <button class="btn btn-primary" id="nextBtn">Next</button>-->
                <button class="btn btn-primary" id="prevBtn"><i class="fa fa-caret-left"></i> Prev</button>
                <span id="pageInfo" class="text-bold"></span>
                <button class="btn btn-primary" id="nextBtn">Next <i class="fa fa-caret-right"></i></button>
            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {
        $('#rf-modal').on('show.bs.modal', function (event) {
            $("#rf-html").html('<string>문서를 불러오는 중입니다...</string>');
            var button = $(event.relatedTarget); // Button that triggered the modal
            var rfId = button.data('id');
            var langTag = button.data('lang');
            $.ajax({
                url: '/rf/view/' + rfId+'?lang=' + langTag,
                method: 'get',
                data: {r: Math.random()},
                success: function (res) {
                    $("#rf-html").html(res);
                }
            });
        });

        var modal;
        var documentId;
        var total = 0;
        var current = 1;
        $('#document-modal').on('show.bs.modal', function (event) {
            current = 1;
            var button = $(event.relatedTarget); // Button that triggered the modal
            documentId = button.data('document-id'); // Extract info from data-* attributes
            total = parseInt(button.data('total')); // Extract info from data-* attributes
            // If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
            // Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
            modal = $(this);
            // modal.find('.modal-title').text('New message to ' + recipient)
            modal.find('#image-view').html("<img src='/sop/${status.name()?lower_case}/viewer/" + documentId + "?page="+current+"' class='img-responsive'/>");
            updatePageInfo();
        });

        $("#prevBtn").click(function() {
            if(current > 1) {
                current --;
                modal.find('#image-view').html("<img src='/sop/${status.name()?lower_case}/viewer/" + documentId + "?page="+current+"' class='img-responsive'/>");

                updatePageInfo();
            }
        });

        $("#nextBtn").click(function() {
            if(total > current) {
                current ++;
                modal.find('#image-view').html("<img src='/sop/${status.name()?lower_case}/viewer/" + documentId + "?page="+current+"' class='img-responsive'/>");
                updatePageInfo();
            }
        });

        function updatePageInfo() {
            if(current == 1) {
                $("#prevBtn").attr("disabled", "disabled");
            } else if(current == 2) {
                $("#prevBtn").removeAttr("disabled");
            }
            if(current == total) {
                $("#nextBtn").attr("disabled", "disabled");
            } else if(current == total - 1) {
                $("#nextBtn").removeAttr("disabled");
            }
            $("#pageInfo").text(current + "/"+total);
        }

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
        // $(document).on("click", "#image-preview img", function(e) {
        //     alert('aaa');
        // });
    });
</script>
</body>
</html>
