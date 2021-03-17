<#setting locale="en_US">
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Billing e.g. password reset</title>
    <link href="http://${domain}/static/plugins/transactional-email-templates/templates/styles.css" media="all"
          rel="stylesheet" type="text/css"/>

    <style type="text/css">
        html, body {
            background-color: #fff !important;
        }
    </style>
</head>

<body itemscope itemtype="http://schema.org/EmailMessage">
<table style="background-color: #ecf0f5; width: 100%;">
    <tr>
        <td></td>
        <td style="width:800px !important; display: block !important; max-width: 800px !important; margin: 0 auto !important; /* makes it centered */ clear: both !important;">
            <div style="max-width: 800px; margin: 0 auto; display: block; padding: 20px;">
                <table width="100%" cellpadding="0" cellspacing="0"
                       style="background-color: #fff; border-bottom: 2px solid #d7d7d7;">
                    <tr>
                        <td style="font-size: 16px; color: #fff; font-weight: 500; padding: 20px; text-align: center; background-color: #8BC34A;">
                            <strong>${message}</strong>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 10px !important; text-align: center;">
                            <table width="100%" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td style="padding: 0 0 20px; text-align: center">
                                        <table style="margin: 25px auto; text-align: left; width: 100%;">
                                            <tr>
                                                <td style="padding: 5px 0;">
                                                    사용자 [${username}]님의 개인정보 활용동의 및 기밀 유지 서약에 대한 철회가 다음과 같이 진행될 예정입니다..
                                                </td>
                                            </tr>
                                            <tr><td>&nbsp;</td></tr>
                                            <tr>
                                                <td style="padding: 5px 0;">
                                                    팀 / 부서 : ${teamDept}
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 5px 0;">
                                                    이름 : ${name}
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 5px 0;">
                                                    철회 예정일자 : <strong>${withdrawalDate?string('yyyy-MM-dd')}</strong>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 0 0 20px; text-align: center">
                                                    <a href="http://${domain}" target="_blank">ISO-MS 바로가기</a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <div style="width: 100%; clear: both; color: #999; padding: 20px;">
                    <table width="100%">
                        <tr>
                            <td style="text-align: center">
                                <small>본 이메일의 발신자 주소는 발신전용으로 회신되지 않습니다.<br/>
                                    ${footerMsg}
                                </small></td>
                        </tr>
                    </table>
                </div>
            </div>
        </td>
        <td></td>
    </tr>
</table>

</body>
</html>
