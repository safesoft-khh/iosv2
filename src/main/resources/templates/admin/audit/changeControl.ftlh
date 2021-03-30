<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<#import "/common/pagination.ftl" as pagination />
<html>
<head>
    <title>Admin > Change Control</title>

    <style>
        .popover {
        . pos-t-l(50 %, 50 %) !important;
        (mixin position top-left) . translate(- 50 %; -50%);
        }

        .btn-justified {
            width:100% !important;
        }

        .table-responsive > table td{
            vertical-align: middle !important;
        }

    </style>
</head>
<body>

<div class="panel panel-bordered-primary">
    <div class="panel-heading">
        <h4 class="panel-title">
            Change Control ::
            <#if audit == 'account'>
            &nbsp;User Profile
            <#elseif audit == 'user-role'>
                &nbsp;User Role Change
            <#elseif audit == 'notice'>
                &nbsp;공지사항
            <#elseif audit == 'certifications'>
                &nbsp;인증 현황
            <#elseif audit == 'document'>
                &nbsp;SOP/${formName}
            <#elseif audit == 'documentVersion'>
                &nbsp;SOP/${formName} Version
            <#elseif audit == 'iso'>
                &nbsp;ISO 14155
            <#elseif audit == 'sop-training'>
                &nbsp;SOP Training
            <#elseif audit == 'iso-training'>
                &nbsp;ISO Training
            <#elseif audit == 'approval'>
                &nbsp;Approvals
            <#elseif audit == 'sop-training-deviation'>
                &nbsp;SOP Training Deviation
            <#elseif audit == 'sop-iso-disclosure'>
                &nbsp;SOP & Training Log Disclosure
            </#if>
        </h4>


        <div class="btn-group btn-group-sm btn-group-justified" role="group">
            <a href="/admin/change-control/account" class="btn btn-mini btn-primary btn-justified ${(audit == 'account')?then('active', '')}" style="border-right: solid silver;">User Profile</a>
            <a href="/admin/change-control/user-role" class="btn btn-mini btn-primary btn-justified ${(audit == 'user-role')?then('active', '')}">User Role Change</a>
            <a href="/admin/change-control/notice" class="btn btn-mini btn-primary btn-justified ${(audit == 'notice')?then('active', '')}">공지사항</a>
            <a href="/admin/change-control/certifications" class="btn btn-mini btn-primary btn-justified ${(audit == 'certifications')?then('active', '')}">인증현황</a>
            <a href="/admin/change-control/document" class="btn btn-mini btn-primary btn-justified ${(audit == 'document')?then('active', '')}">SOP/${formName}</a>
            <a href="/admin/change-control/documentVersion" class="btn btn-mini btn-primary btn-justified ${(audit == 'documentVersion')?then('active', '')}">SOP/${formName} Version</a>
            <a href="/admin/change-control/iso" class="btn btn-mini btn-primary btn-justified ${(audit == 'iso')?then('active', '')}">ISO</a>
            <a href="/admin/change-control/sop-training" class="btn btn-mini btn-primary btn-justified ${(audit == 'sop-training')?then('active', '')}">SOP Training</a>
            <a href="/admin/change-control/iso-training" class="btn btn-mini btn-primary btn-justified ${(audit == 'iso-training')?then('active', '')}">ISO Training</a>

            <a class="btn btn-mini btn-primary btn-justified dropdown-toggle ${(audit == 'sop-training-deviation' || audit == 'sop-iso-disclosure')?then('active', '')}" data-toggle="dropdown" type="button" aria-expanded="false">
                <#if audit == 'approval'>
                    Approvals
                <#elseif audit == 'sop-training-deviation'>
                    SOP Training Deviation
                <#elseif audit == 'sop-iso-disclosure'>
                    SOP & Training Log...
                <#else>
                    전자 결재
                </#if>
                <i class="dropdown-caret"></i>
            </a>
            <ul class="dropdown-menu dropdown-menu-right">
                <li>
                    <a href="/admin/change-control/approval">Approvals</a>
                </li>
                <li>
                    <a href="/admin/change-control/sop-training-deviation">SOP Training Deviation</a>
                </li>
                <li>
                    <a href="/admin/change-control/sop-iso-disclosure">SOP & Training Log Disclosure</a>
                </li>
            </ul>
        </div>
    </div>
    <hr>
    <!--Pagination-->
    <!--===================================================-->
    <div class="panel-body">
        <#include "form/" + viewName + ".ftlh"/>
    </div>
    <!--===================================================-->
    <!--End Pagination-->

</div>
</body>
</html>