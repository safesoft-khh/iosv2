<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta name="viewport" content="width=device-width" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Billing e.g. invoices and receipts</title>
    <link href="http://${domain}/static/plugins/transactional-email-templates/templates/styles.css" media="all" rel="stylesheet" type="text/css" />
</head>

<body itemscope itemtype="http://schema.org/EmailMessage">

<table class="body-wrap" style="background-color: #ecf0f5; width: 100%;">
    <tr>
        <td></td>
        <td class="container" style="display: block !important; max-width: 800px !important; margin: 0 auto !important; clear: both !important;width:800px;max-width:800px !important;">
            <div class="content" style="max-width: 800px;  margin: 0 auto; display: block; padding: 20px; width:800px; max-width:800px !important;">
                <table class="main" width="100%" cellpadding="0" cellspacing="0" style="background-color: #fff; border-bottom: 2px solid #d7d7d7;">
                    <tr>
                        <td class="alert alert-good" style="font-size: 16px; color: #fff; font-weight: 500; padding: 20px; text-align: center; background-color: #8BC34A;">
                            <strong>[ISO MS] Off-line Training 반영 알림</strong>
                        </td>
                    </tr>
                    <tr>
                        <td class="content-wrap aligncenter" style="vertical-align: top; padding: 10px !important; text-align: center;">
                            <table width="100%" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td class="content-block aligncenter">
                                        <table class="invoice" style="margin: 25px auto; text-align: left; width: 100%;">
                                            <!--                                            <tr>-->
                                            <!--                                                <td>[e-SOP] SOP Off-line Training 등록 요청</td>-->
                                            <!--                                            </tr>-->
                                            <tr>
                                                <td>
                                                    <table class="invoice-items" style="width:100%;" cellpadding="0" cellspacing="0">
                                                        <tr>
                                                            <td style="padding: 5px 0;">Training Date</td>
                                                            <td class="alignleft" style="padding: 5px 0; text-align: left;">${offlineTraining.trainingDate?string('yyyy-MM-dd')}</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 5px 0;">Organization</td>
                                                            <td class="alignleft" style="padding: 5px 0; text-align: left;">${offlineTraining.organization}</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 5px 0;">Training SOP</td>
                                                            <td class="alignleft" style="padding: 5px 0; text-align: left;">
                                                                <#list offlineTraining.offlineTrainingDocuments as doc>
                                                                    ${doc.documentVersion.document.docId} ${doc.documentVersion.document.title} v${doc.documentVersion.version} / (${doc.hour}H)<br/>
                                                                </#list>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="content-block aligncenter" style="padding: 0 0 20px; text-align: center;">
                                                    <a href="http://${domain}" target="_blank">${siteCode} ISO-MS 바로가기</a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <div class="footer" style="width: 100%; clear: both; color: #999; padding: 20px;">
                    <table width="100%">
                        <tr>
                            <td class="aligncenter" style="color: #999; font-size: 12px; text-align: center;">
                                <small>본 이메일의 발신자 주소는 발신전용으로 회신되지 않습니다.<br/>
                                    ${footerMsg}
                                </small>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </td>
    </tr>
</table>

</body>
</html>
