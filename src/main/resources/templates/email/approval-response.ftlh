<#setting locale="en_US">
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
        <td class="container" style="display: block !important; max-width: 800px !important; margin: 0 auto !important; clear: both !important;">
            <div class="content" style="max-width: 800px; margin: 0 auto; display: block; padding: 20px;">
                <table class="main" width="100%" cellpadding="0" cellspacing="0" style="background-color: #fff; border-bottom: 2px solid #d7d7d7;">
                    <tr>
                        <td class="alert alert-good" style="vertical-align: top; font-size: 16px; color: #fff; font-weight: 500; padding: 20px; text-align: center; background-color: #8BC34A;">
                            <strong>${message}</strong>
                        </td>
                    </tr>
                    <tr>
                        <td class="content-wrap aligncenter" style="vertical-align: top; padding: 0 0 20px; text-align: center;">
                            <table width="100%" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td class="content-block aligncenter" style="padding: 0 0 20px; text-align: center;">
                                        <table class="invoice" style="margin: 25px auto; text-align: left; width: 100%;">
                                            <!--                                            <tr>-->
                                            <!--                                                <td>SOP Deviation Report 에 대한 승인(or 검토) 요청이 있습니다.</td>-->
                                            <!--                                            </tr>-->
                                            <tr>
                                                <td>
                                                    <#include "approval-email-body.ftlh"/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="content-block" style="padding: 0 0 20px;">
                                                    상세 내용을 확인하여 주십시오.
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
                            <td class="aligncenter" style="text-align: center;">
                                <small>본 이메일의 발신자 주소는 발신전용으로 회신되지 않습니다.<br/>
                                    ${footerMsg}
                                </small></td>
                        </tr>
                    </table>
                </div></div>
        </td>
        <td></td>
    </tr>
</table>

</body>
</html>
