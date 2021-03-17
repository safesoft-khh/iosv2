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
                <div class="col-xs-8 text-right pad-top"><span class="text-bold" style="font-size:25px;">&nbsp;&nbsp;Non-Disclosure Agreement for SOP</span></div>
            </div>
            <hr/>
            <div>&nbsp;</div>
            <div class="mar-top mar-btm">This NON-DISCLOSURE AGREEMENT FOR SOP is to assure the protection and preservation of the confidential and/or proprietary nature of information to be disclosed.</div>
            <div class="mar-btm">The Disclosure (${siteCode}) intends to disclose proprietary information and data <span class="text-bold"><’The Confidential Information’></span> relating to ${siteCode} Standard Operating Procedures (SOP) to the Recipient for the purpose as below (<span style="font-style: italic">Please tick where applicable</span>).</div>

            <div class="mar-top mar-btm">
                <label class="checkbox-inline">
                    <input type="checkbox" disabled${(nonDisclosureAgreement.externalCustomer.sopDisclosureRequestForm.purposeOfDisclosure == 'AUDIT')?then(' checked', '')}> Audit
                </label>
                <label class="checkbox-inline">
                    <input type="checkbox" disabled${(nonDisclosureAgreement.externalCustomer.sopDisclosureRequestForm.purposeOfDisclosure == 'INSPECTION')?then(' checked', '')}> Inspection
                </label>
<#--                <label class="checkbox-inline">-->
<#--                    <input type="checkbox" disabled${(nonDisclosureAgreement.externalCustomer.sopDisclosureRequestForm.purposeOfDisclosure == 'ASSESSMENT')?then(' checked', '')}> Assessment/Evaluation Visit-->
<#--                </label>-->
                <label class="checkbox-inline">
                    <input type="checkbox" disabled${(nonDisclosureAgreement.externalCustomer.sopDisclosureRequestForm.purposeOfDisclosure == 'OTHER')?then(' checked', '')}> Other:${(nonDisclosureAgreement.externalCustomer.sopDisclosureRequestForm.purposeOfDisclosure == 'OTHER')?then(nonDisclosureAgreement.externalCustomer.sopDisclosureRequestForm.purposeOfDisclosureOther, '_______________________')}
                </label>
            </div>

            <div class="text-bold">Obligation of Recipient; </div>
            <div style="font-style: italic;" class="mar-btm"><u>* Please tick the all boxes when you have read and understand the statement below.</u></div>
            <div class="mar-btm text-muted">
                <label class="checkbox-inline">
                    <input type="checkbox" id="chk1"> I understand that the Confidential Information cannot be used for any purpose or in any manner that would constitute a violation of any laws or regulations.
                </label>
            </div>
            <div class="mar-btm text-muted">
                <label class="checkbox-inline">
                    <input type="checkbox" id="chk2"> I agree to keep the Confidential Information secure and not to disclose it to any third party.
                </label>
            </div>
            <div class="mar-btm text-muted">
                <label class="checkbox-inline">
                    <input type="checkbox" id="chk3"> I agree that the Confidential Information provided shall not be reproduced in any form.
                </label>
            </div>
            <div class="mar-btm text-muted">
                <label class="checkbox-inline">
                    <input type="checkbox" id="chk4"> I will return all copies and records of the Confidential Information to the Discloser and will not retain any copies or records of the Confidential Information without permission.
                </label>
            </div>

            <div>&nbsp;</div>
            <div class="mar-top">
                <div class="col-xs-3 text-center">
                    <div style="height:40px;" class="pad-top">${user.engName}</div>
                    <hr style="margin-top:5px;margin-bottom: 3px"/><div class="text-xs">Print Name</div>
                </div>
                <div class="col-xs-3 text-center">
                    <div style="height:40px;" class="pad-top">${user.getRoleAndCompony()!''}</div>
                    <hr style="margin-top:5px;margin-bottom: 3px"/><div class="text-xs">Title/Company</div>
                </div>
                <div class="col-xs-3 text-center">
                    <#if user.signature == true>
                        <div id="sign" style="height:40px;">
                            <img src="${signatureData!''}" width="50px" >
                        </div>
                    <#else>
                        <div id="sign" style="height:40px;"><a class="btn btn-link" href="#sign-modal" data-toggle="modal"><i class="fa fa-pencil"></i> Sign </a></div>
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
                <a class="btn btn-warning" href="/logout"><i class="fa fa-times"></i> Cancel</a>
                <button class="btn btn-info" type="submit"><i class="fa fa-check"></i> Submit</button>
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
                <button class="btn btn-default" type="button" id="clear"><i class="fa fa-eraser"></i> Clear</button>
                <button class="btn btn-info" type="button" id="save"><i class="fa fa-check"></i> Submit</button>
            </div>
        </div>
    </div>
</div>
</body>
</html>
