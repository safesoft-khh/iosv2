<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<@security.authentication property="principal" var="user"/>
<#setting locale="en_US">
<html>
<head>
    <title>ISO Management System</title>
    <script src="/static/js/signature_pad.min.js"></script>
    <script>
        $(document).ready(function () {
            var signaturePad = new SignaturePad(document.getElementById('signature-pad'), {
                backgroundColor: 'rgba(255, 255, 255, 0)',
                penColor: 'rgb(0, 0, 0)',
                minWidth: 3,
                maxWidth: 7
            });

            if ($("#base64signature").val()) {
                signaturePad.fromDataURL($("#base64signature").val());
            }
            var saveButton = document.getElementById('save');
            var cancelButton = document.getElementById('clear');

            saveButton.addEventListener('click', function (event) {
                if (signaturePad.isEmpty()) {
                    event.preventDefault();
                    alert('Please sign.');
                } else {
                    var data = signaturePad.toDataURL('image/png');

                    $("#base64signature").val(data);
                    // $("#agreeForm").submit();
                    $("#sign").html("<img src='" + data + "' width='50px'/>")
                    $("#sign-modal").modal('hide');
                }
            });

            cancelButton.addEventListener('click', function (event) {
                signaturePad.clear();
            });

            $("#agreeForm").submit(function (e) {
                if (!$("#base64signature").val()) {
                    e.preventDefault();
                    alert('Please sign.');
                }
            });
        });
    </script>
    <!--BootstrapJS [ RECOMMENDED ]-->
    <script src="/static/js/bootstrap.min.js"></script>
</head>
<body>


<div class="cls-content">
    <div class="cls-content-lg panel" style="width:750px !important;">
        <div class="panel-body text-left">
            <div class="mar-btm row">
                <div class="col-xs-4"><img src="${imageLogo}" height="45px"/></div>
                <div class="col-xs-8 text-right pad-top"><span class="text-bold" style="font-size:25px;">&nbsp;&nbsp;Internal User Terms Of Use</span>
                </div>
            </div>
            <hr/>
            <div class="panel mar-top mar-btm">

                <div class="mar-btm text-center">
                    <h3>개인 정보 활용 동의</h3>
                </div>
                <div class="panel-body bg-gray" style="height:200px; overflow-y: scroll">
                    <div class="mar-top mar-btm text-bold">
                        1. 본인은 개인정보보호법에 따라 “${siteCompanyKorTitle} ${isoTitle} SOP/RF" 접속을 위해<br>
                        &nbsp;&nbsp;&nbsp;&nbsp;개인정보(이름, 소속, 이메일, 직함/직위)를 제공하고, 시스템에 5년간 보관되며,<br>
                        &nbsp;&nbsp;&nbsp;&nbsp;${isoTitle}시스템 점검 및 관련 활동에 이용되는 것에 동의합니다.<br><br>
                        2. 본인은 개인정보의 수집 및 이용을 거부할 권리가 있지만, 개인정보 제공을 거부할 경우 <br>
                        &nbsp;&nbsp;&nbsp;&nbsp;"${siteCompanyKorTitle} ${isoTitle} SOP/RF"에 대한 접근이 금지될 것에 동의합니다.
                    </div>
                </div>
                <div class="mar-btm text-center">
                    <h3>비밀 보장 서약서</h3>
                </div>
                <div class="panel-body bg-gray" style="height:200px; overflow-y: scroll">
                    <div class="mar-top mar-btm text-bold">
                        1. 본인은 <span class="text-info">ISO Management System</span>에서 열람, 기록 및 관리되는 문서들<br>
                        &nbsp;&nbsp;&nbsp;&nbsp;(예, 표준작업지침서, 관련 서식, ISO 관련 규정, 교육 기록 등)이<br>
                        &nbsp;&nbsp;&nbsp;&nbsp;${siteCompanyKorTitle}의 자산으로 비밀사항임을 이해 합니다.<br><br>
                        2. 본인은 <span class="text-info">ISO Management System</span>에서 열람, 기록 및 관리되는 문서들을<br>
                        &nbsp;&nbsp;&nbsp;&nbsp;${siteCompanyKorTitle}의 허가 또는 승인없이 제 3자에게 복사, 제공 및 배포하지 않을 것입니다.<br>
                        &nbsp;&nbsp;&nbsp;&nbsp;본 서약에 동의함으로써 본인은 정보의 비밀을 준수할 것임을 서약 합니다.
                    </div>
                    <#--                    <div class="mar-btm">The Discloser (KCSG) intends to disclose proprietary information and data <span class="text-bold"><’The Confidential Information’></span> relating to KCSG Standard Operating Procedures (SOP) to the Recipient for the purpose as below (<span style="font-style: italic">Please tick where applicable</span>).</div>-->
                    <#--                    <div class="text-bold">Obligation of Recipient; </div>-->
                    <#--                    <div style="font-style: italic;" class="mar-btm"><u>* Please tick the all boxes when you have read and understand the statement below.</u></div>-->
                </div>
            </div>
            <div class="mar-top">
                <div class="col-xs-3 text-center">
                    <div style="height:40px;" class="pad-top">${user.engName}</div>
                    <hr style="margin-top:5px;margin-bottom: 3px"/><div class="text-xs">Print Name</div>
                </div>
                <div class="col-xs-3 text-center">
                    <div style="height:40px;" class="pad-top">${user.getRoleAndCompony()!''}</div>
                    <hr style="margin-top:5px;margin-bottom: 3px"/><div class="text-xs">Role/Company</div>
                </div>
                <div class="col-xs-3 text-center">
                    <#if user.signature == true>
                        <div id="sign" style="height:40px;">
                            <img src="${signatureData}" width="50px" >
                        </div>
                    <#else>
                        <div id="sign" style="height:40px;"><a class="btn btn-link" href="#sign-modal" data-toggle="modal"><i class="fa fa-pencil"></i> Sign </a> </div>
                    </#if>
                    <hr style="margin-top:5px;margin-bottom: 3px"/><div class="text-xs">Signature</div>
                </div>
                <div class="col-xs-3 text-center">
                    <div style="height:40px;" class="pad-top">${user.loginDate?string('dd-MMM-yyyy')}</div>
                    <hr style="margin-top:5px;margin-bottom: 3px"/><div class="text-xs">Date<span class="text-muted">(DD-MMM-YYYY)</span></div>
                </div>
            </div>
        </div>
        <div class="panel-footer text-right">
            <form method="post" id="agreeForm">
                <input type="hidden" id="base64signature" name="base64signature" value="${signatureData!''}"/>
                <a class="btn btn-warning" href="/logout"><i class="fa fa-times"></i> I do not agree</a>
                <button class="btn btn-info" type="submit"><i class="fa fa-check"></i> I agree</button>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="sign-modal" role="dialog" tabindex="-1" aria-labelledby="sign-modal" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <!--Modal header-->
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><i class="pci-cross pci-circle"></i></button>
                <h4 class="modal-title">Signature</h4>
            </div>

            <!--Modal body-->
            <div class="modal-body" style="padding:0px;">
                <#--                    <div>-->
                <div class="wrapper" style="padding:5px">
                    <canvas id="signature-pad" class="signature-pad" width=585 height=400 style="border:1px solid #eeeeee"></canvas>
                </div>
            </div>

            <!--Modal footer-->
            <div class="modal-footer text-right">
                <#--                    <button data-dismiss="modal" class="btn btn-default" type="button"><i class="fa fa-times"></i> Cle</button>-->
                <button class="btn btn-default" type="button" id="clear"><i class="fa fa-eraser"></i> Clear</button>
                <button class="btn btn-info" type="button" id="save"><i class="fa fa-check"></i> Submit</button>
            </div>
        </div>
    </div>
</div>
</body>
</html>
