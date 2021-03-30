<#import "/spring.ftl" as spring />
<script>
    $(document).ready(function() {
        $("#retirementApprovalForm\\.sopIds,#retirementApprovalForm\\.rdIds").chosen({width:'100%'});

        $("#retirementApprovalForm\\.sopIds,#retirementApprovalForm\\.rdIds").on("change", function(evt, params) {
            if(params.selected) {
                $("#selectedId").val(params.selected);
            } else if(params.deselected) {
                $("#deselectedId").val(params.deselected);
            }
            $("#appForm").append("<input type='hidden' name='_method' value='put'/>").submit();
        });

        // $('#retirementApprovalForm\\.retirementDate').datepicker({
        //     format: "yyyy-mm-dd",
        //     todayBtn: "linked",
        //     autoclose: true,
        //     todayHighlight: true
        // });

        $("#appForm").submit(function() {
            $("#retirementApprovalForm\\.rdIds option").removeAttr("disabled");

            return true;
        });
    });
</script>
<style type="text/css">
    body .chosen-container-multi .chosen-choices li.search-choice, body .chosen-container .chosen-results li.highlighted {
        width:100% !important;
    }
</style>
<#--<input type="hidden" name="_method" value="put"/>-->
<input type="hidden" name="selectedId" id="selectedId"/>
<input type="hidden" name="deselectedId" id="deselectedId"/>
<table class="table table-bordered">
    <tbody>

    <tr>
        <td class="bg-gray-light col-xs-2">Retirement Documents</td>
        <td>
            <table class="table table-bordered">
                <thead>
                <tr class="bg-gray-light">
                    <th class="text-center">Document Type</th>
                    <th class="text-center">SOP/${formName}<br/>Document Id</th>
                    <th>SOP/${formName} Title</th>
                    <th class="text-center">Ver.</th>
                    <th class="text-center">Effective Date</th>
                </tr>
                </thead>
                <tbody>
                <#if approval.retirementApprovalForm.retirementDocuments?has_content>
                    <#list approval.retirementApprovalForm.retirementDocuments as v>
                    <tr>
                        <td class="text-center">${v.document.type.name()}</td>
                        <td class="text-center">${v.document.docId}</td>
                        <td>${v.document.title}</td>
                        <td class="text-center">${v.version}</td>
                        <td class="text-center">${v.effectiveDate?string('dd-MMM-yyyy')}</td>
                    </tr>
                    </#list>
                </#if>
                <tr>
                    <td colspan="5">
                        <h5>SOP</h5>
                        <@spring.formMultiSelect "approval.retirementApprovalForm.sopIds" sopMap "class='form-control'"/>
                        <@spring.showErrors "<br/>" "color:red;"/>
                        <h5>${formName}</h5>
                        <@spring.formMultiSelect "approval.retirementApprovalForm.rdIds" rdMap "class='form-control'"/>
                        <@spring.showErrors "<br/>" "color:red;"/>
                    </td>
                </tr>
                </tbody>
            </table>
            <#if approval.retirementApprovalForm.sopRdIds?has_content>
                <script>
                <#list approval.retirementApprovalForm.sopRdIds as id>
                    $("#retirementApprovalForm\\.rdIds option[value='${id}']").attr("disabled", "disabled");
                </#list>
                </script>
            </#if>
        </td>
    </tr>
    <tr>
        <td class="bg-gray-light">Reason</td>
        <td>
            <@spring.formTextarea "approval.retirementApprovalForm.reason" "class='form-control'"/>
            <@spring.showErrors "<br/>" "color:red;"/>
        </td>
    </tr>
<#--    <tr>-->
<#--        <td class="bg-gray-light">Retirement Date</td>-->
<#--        <td>-->
<#--            <@spring.formInput "approval.retirementApprovalForm.retirementDate" "class='form-control'"/>-->
<#--            <@spring.showErrors "<br/>" "color:red;"/>-->
<#--        </td>-->
<#--    </tr>-->
    </tbody>
</table>