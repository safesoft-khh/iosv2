<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#import "/spring.ftl" as spring />
<html>
<head>
    <title>ISO > Category 관리</title>
    <script>
        $(document).ready(function(){

            //삭제 버튼 동적 이벤트 할당.
            $(document).on("click", "#deleteBtn", function(){
                let CategoryName = $(this).data("value");
                let CategoryId = $(this).data("id");

                let res = confirm("정말로 [" + CategoryName + "]을(를) 삭제하시겠습니까?");
                if(res) {
                    $("#categoryId").val(CategoryId);
                    $("#deleteForm").submit();
                }
            });
        })
    </script>

</head>
<body>

<div class="panel">
    <#--    <div class="panel-heading">-->
    <#--        <h3 class="panel-title">Category List</h3>-->
    <#--    </div>-->

    <!--Data Table-->
    <!--===================================================-->
    <div class="panel-body">
        <div class="pad-btm form-inline">
            <div class="row">
                <div class="col-sm-6 table-toolbar-left">
                    <a class="btn btn-primary" href="/iso/category?action=new#edit0"><i class="pli-pencil"></i> Category 추가</a>
                </div>
                <div class="col-sm-6 table-toolbar-right">
                    <#--                    <div class="form-group">-->
                    <#--                        <input id="demo-input-search2" type="text" placeholder="Search" class="form-control" autocomplete="off">-->
                    <#--                    </div>-->
                    <#--                    <div class="btn-group">-->
                    <#--                        <button class="btn btn-default"><i class="fa fa-search fa-lg"></i></button>-->
                    <#--                    </div>-->
                </div>
            </div>
        </div>
        <div class="table-responsive">
            <form name="f" method="post" action=/iso/category>
                <input type="hidden" name="action" value="${action}">
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th class="text-center" style="width:70px;">No.</th>
                        <th>Category ID.</th>
                        <th>Category Name</th>
                        <th class="text-center">#actions</th>
                    </tr>
                    </thead>
                    <tbody>

                    <#if CategoryList?has_content>
                        <#list CategoryList as Category>
                            <tr>
                                <td class="text-center">${Category?index + 1}
                                    <a name="edit${Category?index}"></a>
                                    <#if action == "edit" && id == Category.id>
                                        <@spring.formHiddenInput "Category.id"/>
                                    </#if>
                                </td>
                                <td>
                                    <#if action == "edit" && id == Category.id>
                                        <#assign readonly = Category.readonly?then(" readonly='readonly'", "")/>
                                        <@spring.formInput "Category.shortName" "class='form-control' ${readonly}"/>
                                    <#else>
                                        ${Category.shortName}
                                    </#if>
                                </td>
                                <td>
                                    <#if action == "edit" && id == Category.id>
                                        <@spring.formInput "Category.name" "class='form-control'"/>
                                    <#else>
                                        ${Category.name}
                                    </#if>
                                </td>
                                <td class="text-center">
                                    <#if action == "edit" && id == Category.id>
                                        <button class="btn btn-primary" type="submit"><i class="fa fa-check"></i> 저장</button>
                                    <#else>
                                        <a class="btn btn-sm btn-mint" href="/iso/category?action=edit&id=${Category.id}#edit${Category?index}"><i class="fa fa-pencil"> 수정</i></a>
                                        <button class="btn btn-sm btn-danger" data-value="${Category.name}" data-id="${Category.id}" type="button" id="deleteBtn"><i class="fa fa-trash"> 삭제</i></button>
                                    </#if>
                                </td>
                            </tr>
                        </#list>
                    <#else>
                        <tr>
                            <td colspan="4" class="text-center">등록된 카테고리가 존재하지 않습니다.</td>
                        </tr>
                    </#if>
                    <#if action == "new">
                        <tr>
                            <td class="text-center">#<a name="edit0"></a></td>
                            <td>
                                <@spring.formInput "Category.shortName" "class='form-control' placeholder='Short Name'"/>
                                <@spring.showErrors "" "color:red;"/></td>
                            <td>
                                <@spring.formInput "Category.name" "class='form-control' placeholder='Name'"/>
                                <@spring.showErrors "" "color:red;"/></td>
                            <td class="text-center">
                                <button class="btn btn-primary" type="submit"><i class="fa fa-check"></i> 저장</button>
                            </td>
                        </tr>
                    </#if>
                    </tbody>
                </table>
            </form>
            <form method="post" id="deleteForm">
                <input type="hidden" name="_method" value="delete" />
                <input type="hidden" name="id" id="categoryId" />
            </form>
        </div>
    </div>
    <!--===================================================-->
    <!--End Data Table-->

</div>
</body>
</html>