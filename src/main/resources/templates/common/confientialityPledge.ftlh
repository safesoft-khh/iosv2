<#assign security=JspTaglibs["http://www.springframework.org/security/tags"] />
<@security.authentication property="principal" var="user"/>
<#setting locale="en_US">
<html>
<head>
    <title>ISO Management System</title>
    <script src="/static/js/signature_pad.min.js"></script>
    <script>
        $(document).ready(function() {
            var signaturePad = new SignaturePad(document.getElementById('signature-pad'), {
                backgroundColor: 'rgba(255, 255, 255, 0)',
                penColor: 'rgb(0, 0, 0)',
                minWidth:3,
                maxWidth:7
            });

            if($("#base64signature").val()) {
                signaturePad.fromDataURL($("#base64signature").val());
            }
            var saveButton = document.getElementById('save');
            var cancelButton = document.getElementById('clear');

            saveButton.addEventListener('click', function (event) {
                if(signaturePad.isEmpty()) {
                    event.preventDefault();
                    alert('Please sign.');
                } else {
                    var data = signaturePad.toDataURL('image/png');

                    $("#base64signature").val(data);
                    // $("#agreeForm").submit();
                    $("#sign").html("<img src='"+data+"' width='50px'/>")
                    $("#sign-modal").modal('hide');
                }
            });

            cancelButton.addEventListener('click', function (event) {
                signaturePad.clear();
            });

            $("#agreeForm").submit(function(e) {
                if(!$("#chk1").is(":checked") ||
                    !$("#chk2").is(":checked") ||
                    !$("#chk3").is(":checked") ||
                    !$("#chk4").is(":checked")) {
                    e.preventDefault();
                    alert('Please tick the all boxes when you have read and understand the statement below');
                } else if(!$("#base64signature").val()) {
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
                <div class="col-xs-8 text-right pad-top"><span class="text-bold" style="font-size:25px;">&nbsp;&nbsp;Confidentiality Pledge</span></div>
            </div>
            <hr/>
            <div class="panel mar-top mar-btm">
                <div class="panel-body bg-gray" style="height:400px; overflow-y: scroll">
                    <div class="mar-btm text-center">
                        <h3>비밀 보장 서약서</h3>
                    </div>
                    <div class="mar-top mar-btm text-bold">
                        본인은 <span class="text-info">ISO Management System</span>에서 열람, 기록 및 관리되는 문서들<br>
                        (예, 표준작업지침서, 관련 서식, ISO 관련 규정, 교육 기록 등)이<br>
                        ${siteCompanyKorTitle}의 자산으로 비밀사항임을 이해 합니다.<br><br>
                        본인은 <span class="text-info">ISO Management System</span>에서 열람, 기록 및 관리되는 문서들을<br>
                        ${siteCompanyKorTitle}의 허가 또는 승인없이 제 3자에게 복사, 제공 및 배포하지 않을 것입니다.<br>
                        본 서약에 동의함으로써 본인은 정보의 비밀을 준수할 것임을 서약 합니다.
                    </div>
<#--                    <div class="mar-btm">The Discloser (KCSG) intends to disclose proprietary information and data <span class="text-bold"><’The Confidential Information’></span> relating to KCSG Standard Operating Procedures (SOP) to the Recipient for the purpose as below (<span style="font-style: italic">Please tick where applicable</span>).</div>-->
<#--                    <div class="text-bold">Obligation of Recipient; </div>-->
<#--                    <div style="font-style: italic;" class="mar-btm"><u>* Please tick the all boxes when you have read and understand the statement below.</u></div>-->
                </div>
            </div>
        </div>
        <div class="panel-footer text-right">
            <form method="post" id="agreeForm">
                <input type="hidden" id="base64signature" name="base64signature" value="${signatureData!''}"/>
                <a class="btn btn-warning" href="/logout"><i class="fa fa-times"></i> Cancel</a>
                <button class="btn btn-info" type="submit"><i class="fa fa-check"></i> Submit</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>
