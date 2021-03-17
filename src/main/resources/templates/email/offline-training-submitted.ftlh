<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta name="viewport" content="width=device-width" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Billing e.g. invoices and receipts</title>
    <link href="http://${domain}/static/plugins/transactional-email-templates/templates/styles.css" media="all" rel="stylesheet" type="text/css" />
</head>

<body itemscope itemtype="http://schema.org/EmailMessage">

<table class="body-wrap">
    <tr>
        <td></td>
        <td class="container" style="width:800px;max-width:800px !important;">
            <div class="content" style="width:800px;max-width:800px !important;">
                <table class="main" width="100%" cellpadding="0" cellspacing="0">
                    <tr>
                        <td class="alert alert-good" style="font-size: 16px; color: #fff; font-weight: 500; padding: 20px; text-align: center; background-color: #8BC34A;">
                            <strong>[ISO-MS] Off-line Training 등록 요청</strong>
                        </td>
                    </tr>
                    <tr>
                        <td class="content-wrap aligncenter">
                            <table width="100%" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td class="content-block aligncenter">
                                        <table class="invoice" style="margin: 25px auto; text-align: left; width: 100%;">
                                            <!--                                            <tr>-->
                                            <!--                                                <td>[e-SOP] SOP Off-line Training 등록 요청</td>-->
                                            <!--                                            </tr>-->
                                            <tr>
                                                <td>
                                                    <table class="invoice-items" cellpadding="0" cellspacing="0">
                                                        <tr>
                                                            <td>Requester</td>
                                                            <td class="alignleft">${requester.name}</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Training Date</td>
                                                            <td class="alignleft">${offlineTraining.trainingDate?string('yyyy-MM-dd')}</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Organization</td>
                                                            <td class="alignleft">${offlineTraining.organization}</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Training SOP</td>
                                                            <td class="alignleft">
<#--                                                                <table width="100%">-->
<#--                                                                    <tr>-->
<#--                                                                        <th>SOP</th>-->
<#--                                                                        <th class="aligncenter">Training Hr.</th>-->
<#--                                                                    </tr>-->
                                                                    <#list offlineTraining.offlineTrainingDocuments as doc>
<#--                                                                    <tr>-->
                                                                        ${doc.documentVersion.document.docId} ${doc.documentVersion.document.title} v${doc.documentVersion.version} / (${doc.hour}H)<br/>
                                                                    </#list>
                                                                <!--                                                                 / 1.0<br/>-->
                                                                <!--                                                                SOP-QM0001 Audit Procedure v1.0 / 1.0<br/>-->
                                                                <!--                                                                SOP-QM0001 Audit Procedure v1.0 / 1.0<br/>-->
                                                                <!--                                                                SOP-QM0001 Audit Procedure v1.0 / 1.0-->
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td >Trainee</td>
                                                            <td class="alignleft"><#list attendees as attendee><#if attendee?is_first == false>, </#if>${attendee.name?default('')}</#list>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="content-block aligncenter" style="padding: 0 0 20px; text-align: center">
                                                    <a href="http://${domain}" target="_blank">ISO MS 바로가기</a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <div class="footer">
                    <table width="100%">
                        <tr>
                            <td class="aligncenter">
                                <small>본 이메일의 발신자 주소는 발신전용으로 회신되지 않습니다.<br/>
                                    ${footerMsg}
                                </small></td>
                        </tr>
                    </table>
                </div></div>
        </td>
        <!--        <td></td>-->
    </tr>
</table>

</body>
</html>
