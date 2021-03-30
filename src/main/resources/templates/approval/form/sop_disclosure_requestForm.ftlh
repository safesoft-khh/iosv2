<#import "/spring.ftl" as spring />
<link href="/static/css/hummingbird-treeview.css" rel="stylesheet">
<script src="/static/js/hummingbird-treeview.js"></script>
<script src="/static/plugins/chosen/chosen.jquery.min.js"></script>
<link href="/static/plugins/chosen/chosen.min.css" rel="stylesheet">
<script>
    $(document).ready(function() {
        // $("#sopDisclosureRequestForm\\.sopIds,#sopDisclosureRequestForm\\.rdIds,#sopDisclosureRequestForm\\.purposeOfDisclosure,#sopDisclosureRequestForm\\.documentAccess").chosen({width:'100%'});
        $("#sopDisclosureRequestForm\\.purposeOfDisclosure,#sopDisclosureRequestForm\\.documentAccess").chosen({width:'100%'});

        $('#training-period .input-daterange').datepicker({
            format: "yyyy-mm-dd",
            todayBtn: "linked",
            autoclose: true,
            todayHighlight: true
        });

        $("#sopDisclosureRequestForm\\.documentAccess").change(function() {
           if($(this).val() == 'OTHER') {
               $("#sopDisclosureRequestForm\\.documentAccessOther").removeAttr("disabled");
           } else {
               $("#sopDisclosureRequestForm\\.documentAccessOther").attr("disabled", "disabled");
           }
        });
        $("#sopDisclosureRequestForm\\.purposeOfDisclosure").change(function() {
           if($(this).val() == 'OTHER') {
               $("#sopDisclosureRequestForm\\.purposeOfDisclosureOther").removeAttr("disabled");
           } else {
               $("#sopDisclosureRequestForm\\.purposeOfDisclosureOther").attr("disabled", "disabled");
           }
        });

        $("#sopDisclosureRequestForm\\.purposeOfDisclosure,#sopDisclosureRequestForm\\.documentAccess").trigger("change");

        $(document).on("click", "button[name='addExternalCustomer']", function() {
            var data = $("#appForm").serialize();
            data += "&_method=put&act=add&r=" + Math.random();
            $.ajax({
                url:'/ajax/approval/box/requester/SOP_Disclosure_Request_Form/externalCustomer',
                method:'post',
                data:data,
                success:function(responseHtml) {
                    // console.log(res);
                    $("#externalCustomerBody").html(responseHtml);
                }
            });
        });
        $(document).on("click", "button[name='removeExternalCustomer']", function() {
            var data = $("#appForm").serialize();
            data += "&_method=put&act=remove&r=" + Math.random()+"&idx="+$(this).data("id");
            $.ajax({
                url:'/ajax/approval/box/requester/SOP_Disclosure_Request_Form/externalCustomer',
                method:'post',
                data:data,
                success:function(responseHtml) {
                    // console.log(res);
                    $("#externalCustomerBody").html(responseHtml);
                }
            });



        });

        // $("#appForm").submit(function(e) {
        //     var str = $("#appForm").serialize().split("&");
        //     for(var i = 0; i < str.length; i ++) {
        //         console.log(str[i]);
        //     }
        //    e.preventDefault();
        // });

        $('select.multi-select').chosen({width:'100%'});
    });
</script>
<style type="text/css">
    body .chosen-container-multi .chosen-choices li.search-choice, body .chosen-container .chosen-results li.highlighted {
        width:100% !important;
    }
</style>
<h5>Requester Information</h5>
<table class="table table-bordered">
    <tbody>
    <tr>
        <td class="bg-gray-light col-xs-3">Name of Requester</td>
        <td><@spring.formInput "approval.sopDisclosureRequestForm.nameOfRequester" "class='form-control' readonly='readonly'"/>
            <@spring.showErrors "<br/>" "color:red;"/>
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Team/Department</td>
        <td><@spring.formInput "approval.sopDisclosureRequestForm.teamDept" "class='form-control' readonly='readonly'"/>
            <@spring.showErrors "<br/>" "color:red;"/>
        </td>
    </tr>
<#--    <tr>-->
<#--        <td class="bg-gray-light">Protocol Title or No.</td>-->
<#--        <td><@spring.formInput "approval.sopDisclosureRequestForm.protocolTitleOrNo" "class='form-control'"/>-->
<#--            <@spring.showErrors "<br/>" "color:red;"/>-->
<#--        </td>-->
<#--    </tr>-->
<#--    <tr>-->
<#--        <td class="bg-gray-light">Project Manager or Team Manager</td>-->
<#--        <td><@spring.formInput "approval.sopDisclosureRequestForm.projectOrTeamManager" "class='form-control'"/>-->
<#--            <@spring.showErrors "<br/>" "color:red;"/>-->
<#--        </td>-->
<#--    </tr>-->
    </tbody>
</table>
<h5>External Customer Information</h5>
<table class="table table-bordered">
    <tbody>
    <tr>
        <td class="bg-gray-light col-xs-3">Company Name or Institute Name</td>
        <td>
            <@spring.formInput "approval.sopDisclosureRequestForm.companyNameOrInstituteName" "class='form-control'"/>
            <@spring.showErrors "<br/>" "color:red;"/>
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">External Customer Name, Email, Role</td>
        <td>
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>No.</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>#</th>
                </tr>
                </thead>
                <tbody id="externalCustomerBody">
                <#include "inc_externalCustomer.ftlh"/>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Request Date<br/><small>(Date or Period to access ISO Management System)</small></td>
        <td>
            <div id="training-period">
                <div class="input-daterange input-group" id="datepicker">
                    <@spring.formInput "approval.sopDisclosureRequestForm.requestStartDate" "class='form-control' placeholder='Start Date'"/>
                    <span class="input-group-addon">to</span>
                    <@spring.formInput "approval.sopDisclosureRequestForm.requestEndDate" "class='form-control' placeholder='End Date'"/>
                </div>
            </div>
            <@spring.showErrors "<br/>" "color:red;"/>
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Requested Documents</td>
        <td>
            <div>
                <h6>[SOP/${formName}]</h6>
            </div>
            <#macro bind path>
                <#if htmlEscape?exists>
                    <#assign status2 = springMacroRequestContext.getBindStatus(path, htmlEscape)>
                <#else>
                    <#assign status2 = springMacroRequestContext.getBindStatus(path)>
                </#if>
            <#-- assign a temporary value, forcing a string representation for any
            kind of variable. This temp value is only used in this macro lib -->
                <#if status2.value?exists && status2.value?is_boolean>
                    <#assign stringStatusValue=status2.value?string>
                <#else>
                    <#assign stringStatusValue=status2.value?default("")>
                </#if>
            </#macro>
            <#macro checkbox path val ids="">
                <@bind path />
            <#--                <#assign id="${status.expression?replace('[','')?replace(']','')}">-->
                <#assign isSelected = ids?has_content && ids?seq_contains(val)/>
                <input type="hidden" name="_${status2.expression}" value="on"/>
                <input type="checkbox" name="${status2.expression}" id="${val}" data-id="${val}" value="${val}"<#if isSelected> checked="checked"</#if> />
            </#macro>

            <#--            <div class="panel panel-default panel-bordered-default">-->
            <#--                <div class="panel-heading">-->
            <#--                    <h5 class="panel-title">Current SOP</h5>-->
            <#--                </div>-->
            <#--                <div class="panel-body">-->
            <ul id="treeview1" style="list-style: none;padding-left:5px;margin-left:0px;">
                <li class="checkbox"> <i class="fa fa-plus"></i>
                    <label><input type="checkbox" />Effective SOP </label>
                    <ul style="list-style: none;padding-left:20px;">

                        <#list CategoryList as c>
                            <#if currentSopCategoryMap[c.id]?has_content>
                                <li class="checkbox"> <i class="fa fa-plus"></i>
                                    <label><input id="${c.id}" data-id="${c.id}" name="ctg" value="${c.id}" type="checkbox" />${c.name} </label>
                                    <ul style="list-style: none;display: none;padding-left:20px;">
                                        <#list currentSopCategoryMap[c.id] as sop>
                                            <li>
                                        <#--                                <i class="fa fa-plus"></i>-->
                                            <label>
                                                <#--                                    <input  id="${sop.id}" data-id="${sop.id}" value="${sop.id}" name="sop" type="checkbox" />-->
                                                <@checkbox "approval.sopDisclosureRequestForm.sopIds" sop.id approval.sopDisclosureRequestForm.sopIds/>
                                                <span class="label label-purple">SOP</span> ${sop.document.docId}/${sop.document.title}/${sop.version}</label>
                                            <#if sop.document.rfList?has_content>
                                            <#--                                    <ul style="list-style: none;">-->
                                                <#list sop.document.rfList as rf>
                                                    <#list rf.documentVersionList as rfVer>
                                                        <#if rfVer.status == 'EFFECTIVE'>
                                                            <li style="padding-left:20px;">
                                                                <label>
                                                                    <#--                                            <input class="hummingbirdNoParent" id="${rdVer.id}" data-id="${rdVer.id}" name="rd" value="${rdVer.id}" type="checkbox" />-->
                                                                    <#--                                            <@spring.formCheckbox "approval.sopDisclosureRequestForm.rdIds" "value='${rdVer.id}'"/>-->
                                                                    <@checkbox "approval.sopDisclosureRequestForm.rfIds" rfVer.id approval.sopDisclosureRequestForm.rfIds/>
                                                                    <span class="label label-info">${formName}</span> ${rfVer.document.docId}/${rfVer.document.title}/${rfVer.version} </label>
                                                            </li>
                                                        </#if>
                                                    </#list>
                                                </#list>
                                            <#--                                </ul>-->
                                            </#if>
                                            </li>
                                        </#list>
                                    </ul>
                                </li>
                            </#if>
                        </#list>
                    </ul>
                </li>
                <li class="checkbox"> <i class="fa fa-plus"></i>
                    <label><input type="checkbox" />Superseded SOP </label>
                    <ul style="list-style: none;padding-left:20px;">
                        <#list CategoryList as c>
                            <#if supersededSopCategoryMap[c.id]?has_content>
                                <li class="checkbox"> <i class="fa fa-plus"></i>
                                    <label><input id="${c.id}" data-id="${c.id}" name="ctg" value="${c.id}" type="checkbox" />${c.name} </label>
                                    <ul style="list-style: none;display: none;padding-left:20px;">
                                        <#list supersededSopCategoryMap[c.id] as sop>
                                            <li>
                                        <#--                    <i class="fa fa-plus"></i>-->
                                            <label>
                                                <#--                                    <input  id="${sop.id}" data-id="${sop.id}" value="${sop.id}" name="sop" type="checkbox" />-->
                                                <#--                                    <@spring.formCheckbox "approval.sopDisclosureRequestForm.sopIds" "value='${sop.id}'"/>-->
                                                <@checkbox "approval.sopDisclosureRequestForm.sopIds" sop.id approval.sopDisclosureRequestForm.sopIds/>
                                                <span class="label label-purple">SOP</span> ${sop.document.docId}/${sop.document.title}/${sop.version}</label>
                                            <#if sop.document.rdList?has_content>
                                            <#--                                    <ul style="list-style: none;">-->
                                                <#list sop.document.rdList as rd>
                                                    <#list rd.documentVersionList as rdVer>
                                                        <#if rdVer.status == 'SUPERSEDED'>
                                                            <li style="padding-left:20px;">
                                                                <label>
                                                                    <#--                                            <input class="hummingbirdNoParent" id="${rdVer.id}" data-id="${rdVer.id}" name="rd" value="${rdVer.id}" type="checkbox" />-->
                                                                    <#--                                            <@spring.formCheckbox "approval.sopDisclosureRequestForm.rdIds" "value='${rdVer.id}'"/>-->
                                                                    <@checkbox "approval.sopDisclosureRequestForm.rfIds" rdVer.id approval.sopDisclosureRequestForm.rfIds/>
                                                                    <span class="label label-info">${formName}</span> ${rdVer.document.docId}/${rdVer.document.title}/${rdVer.version} </label>
                                                            </li>
                                                        </#if>
                                                    </#list>
                                                </#list>
                                            <#--                                </ul>-->
                                            </#if>
                                            </li>
                                        </#list>
                                    </ul>
                                </li>
                            </#if>
                        </#list>
                    </ul>
                </li>
            </ul>

            <hr/>
            <div>
                <h6>[Training Log(SOP)]</h6>
                <div>
                    <@spring.formMultiSelect "approval.sopDisclosureRequestForm.sopUserIds" userMap "class='form-control multi-select'"/>
                </div>
            </div>
            <div>
                <h6>[Training Log(ISO)]</h6>
                <div>
                    <@spring.formMultiSelect "approval.sopDisclosureRequestForm.isoUserIds" userMap "class='form-control multi-select'"/>
                </div>
            </div>
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Type of Document Access</td>
        <td>
            <@spring.formSingleSelect "approval.sopDisclosureRequestForm.documentAccess" documentAccessMap "class='form-control'"/>
            <@spring.showErrors "<br/>" "color:red;"/>
            <@spring.formInput "approval.sopDisclosureRequestForm.documentAccessOther" "class='form-control' placeholder='if other, Please specify'"/>
            <@spring.showErrors "<br/>" "color:red;"/>
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Purpose of Disclosure</td>
        <td>
            <@spring.formSingleSelect "approval.sopDisclosureRequestForm.purposeOfDisclosure" purposeOfDisclosureMap "class='form-control'"/>
            <@spring.showErrors "<br/>" "color:red;"/>
            <@spring.formInput "approval.sopDisclosureRequestForm.purposeOfDisclosureOther" "class='form-control' placeholder='if other, Please specify'"/>
            <@spring.showErrors "<br/>" "color:red;"/>
        </td>
    </tr>
    </tbody>
</table>
<script>

    // Collapsed Symbol
    $.fn.hummingbird.defaults.collapsedSymbol= "fa-folder";

    // Expand Symbol
    $.fn.hummingbird.defaults.expandedSymbol= "fa-folder-open";

    // Set this to "disabled" to disable all checkboxes from nodes that are parents
    $.fn.hummingbird.defaults.checkboxesGroups= "enabled";

    // Enable the functionality to account for disabled nodes
    // $.fn.hummingbird.defaults.checkDisabled= true;

    // Collapse all nodes on init
    $.fn.hummingbird.defaults.collapseAll= true;

    // Enable checkboxes
    $.fn.hummingbird.defaults.checkboxes= "enabled";
    $("#treeview1").hummingbird();

    <#if approval.sopDisclosureRequestForm.sopIds?has_content>
    <#list approval.sopDisclosureRequestForm.sopIds as sopId>
    // expand a specific node
    $("#treeview1").hummingbird("expandNode",{
        attr:"id",
        name: "${sopId}",
        expandParents:true
    });
    </#list>
    </#if>
    <#if approval.sopDisclosureRequestForm.rdIds?has_content>
    <#list approval.sopDisclosureRequestForm.rdIds as rdId>
    // expand a specific node
    $("#treeview1").hummingbird("expandNode",{
        attr:"id",
        name: "${rdId}",
        expandParents:true
    });
    </#list>
    </#if>

</script>