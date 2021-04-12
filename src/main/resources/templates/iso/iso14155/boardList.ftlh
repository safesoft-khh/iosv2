<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#import "/common/pagination.ftl" as pagination />
<html>
<head>
    <title> ISO Management System > ISO > ISO-14155</title>
    <#--    <script src="/static/plugins/jstree/jstree.min.js"></script>-->
    <#--    <link href="/static/plugins/jstree/themes/default/style.min.css" rel="stylesheet">-->

    <!--Bootbox Modals [ OPTIONAL ]-->
    <script src="/static/plugins/bootbox/bootbox.min.js"></script>

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

<div class="tab-base">
    <ul class="nav nav-tabs">
        <li class="active">
            <a href="/iso-14155/board?categoryCode=${categoryCode}">Board</a>
        </li>
        <li>
            <a href="/iso-14155/training?categoryCode=${categoryCode}">Training</a>
        </li>
    </ul>
    <div class="tab-content">
        <div class="pad-btm form-inline">
            <div class="row">

                    <form name="f" id="f" class="form-inline">
                        <#if categoryList?has_content>
                            <div class="box-inline mar-btm">
                                <select name="categoryCode" id="categoryCode" class="form-control">
                                    <option value=""}>ALL</option>
                                    <#list categoryList as category>
                                        <#if categoryCode?has_content&&categoryCode == category.getShortName()>
                                            <option value="${category.getShortName()}" selected>${category.getShortName()}</option>
                                        <#else>
                                            <option value="${category.getShortName()}">${category.getShortName()}</option>
                                        </#if>
                                    </#list>
                                </select>
                                <a class="col-sm-12 table-toolbar-left">
                                    <@security.authorize access="hasAnyAuthority('ADMIN')">
                                        <a class="btn btn-primary" onclick="registfunction()"><i
                                                    class="pli-pencil"></i> 등록</a>
                                    </@security.authorize>

                                    <script>
                                        function registfunction(){
                                            var code = document.getElementById("categoryCode").value;
                                            if(code != "")
                                                location.href="/iso-14155/board/new?categoryCode="+code+"";
                                            else
                                                alert("Category를 선택해 주세요");
                                        }
                                    </script>

                                </a>
                            </div>

                            <script>
                                $("#categoryCode").change(function() {
                                    document.getElementById("categoryCode").value;
                                    //$("#categoryCode").val("");
                                    $("#f").attr("method", "get").submit();
                                });
                            </script>
                        </#if>
                    </form>




            </div>
        </div>
        <@pagination.bind isoList/>
<#--        <#if categoryCode?has_content>-->
<#--            <input type="hidden"  id="categoryCode", name = "categoryCode" value="${categoryCode}">-->
<#--        </#if>-->



        <#--        <div class="table-responsive">-->
        <table class="table">
            <thead>
            <tr>
                <th class="text-center" style="width:70px;">No.</th>
                <th class="text-center" style="width:90px;">Type</th>
                <th class="text-center" style="width:90px;">Category</th>
                <th>Title</th>
                <th class="text-center" style="width:160px;">등록일</th>
                <th class="text-center" style="width:140px;">작성자</th>
            </tr>
            </thead>
            <tbody>
            <#if topISOList?has_content>
                <#list topISOList as iso>
                    <tr class="bg-gray-light">
                        <td class="text-center"><i class="fa fa-exclamation-circle text-warning"></i></td>
                        <td class="text-center"><span class="label label-${iso.training?then('info','mint')}">${iso.training?then('Training','Board')}</span></td>

                        <#if iso.category?has_content>
                            <td class="text-center"><span class="label label-pink">${iso.getCategory().getShortName()}</span></td>
                            <td><a class="btn-link" href="/iso-14155/${iso.id}?categoryCode=${iso.getCategory().getShortName()}">${iso.title}</a></td>
                        <#else>
                            <td class="text-center"><span class="label label-pink"></span></td>
                            <td><a class="btn-link" href="/iso-14155/${iso.id}">${iso.title}</a></td>
                        </#if>
                        <td class="text-center"><span class="text-muted">${iso.createdDate?string('yyyy-MM-dd')}</span></td>
                        <td class="text-center">${iso.createdBy?default('System')}</td>
                    </tr>
                </#list>
            </#if>
            <#if isoList.content?has_content>
                <#list isoList.content as iso>
                    <tr>
                        <td class="text-center">${(isoList.totalElements - (isoList.size * isoList.number)) - iso?index}</td>
                        <td class="text-center"><span class="label label-${iso.training?then('info','mint')}">${iso.training?then('Training','Board')}</span></td>

                        <#if iso.category?has_content>
                            <td class="text-center"><span class="label label-pink">${iso.getCategory().getShortName()}</span></td>
                            <td><a class="btn-link" href="/iso-14155/${iso.id}?categoryCode=${iso.getCategory().getShortName()}">${iso.title}</a></td>
                        <#else>
                            <td class="text-center"><span class="label label-pinkd"></span></td>
                            <td><a class="btn-link" href="/iso-14155/${iso.id}">${iso.title}</a></td>
                        </#if>

                        <td>
                            <a class="btn-link" href="/iso-14155/${iso.id}">${iso.title}</a>
                        </td>
                        <td class="text-center"><span class="text-muted">${iso.createdDate?string('yyyy-MM-dd')}</span></td>
                        <td class="text-center">${iso.createdBy?default('System')}</td>
                    </tr>
                </#list>
            <#else>
                <tr>
                    <td colspan="5" class="text-center">등록된 ISO-14155가 없습니다.</td>
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