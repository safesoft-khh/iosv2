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
                    $("#sign").html("<img src='"+data+"' width='50px'/>")
                    $("#sign-modal").modal('hide');
                }
            });

            cancelButton.addEventListener('click', function (event) {
                signaturePad.clear();
            });

            $("#agreeForm").submit(function(e) {
                if(!$("#base64signature").val()) {
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
                <div class="col-xs-8 text-right pad-top"><span class="text-bold text-lg">&nbsp;&nbsp;Agreement to Collect and Use Personal Information</span></div>
            </div>
            <hr/>
            <div>&nbsp;</div>
            <div class="mar-top mar-btm">I fully understand the contents written below, and agree to the collection and use of personal information in accordance with the Personal Information Protection Act.</div>

            <div class="text-bold">1. Purpose of Collection and Use of Personal Information</div>
            <div class="mar-btm text-muted">&nbsp;&nbsp;&nbsp;&nbsp;To access “${siteCompanyTitle}${isoTitle?has_content?then('-', '')}${isoTitle}” SOP/RF as per request by the External Customer.)</div>

            <div class="text-bold">2. Items of Personal Information to be Collected and Used</div>
            <div class="mar-btm text-muted">&nbsp;&nbsp;&nbsp;&nbsp;: Name, Affiliation, E-mail and Role.</div>

            <div class="text-bold">3. Period to Retain and Use of Personal Information</div>
            <div class="mar-btm text-muted">&nbsp;&nbsp;&nbsp;&nbsp;: 5 years from the date of the Agreement.</div>

            <div class="text-bold">4. Provision of Personal Information to the Third Parties</div>
            <div class="mar-btm text-muted">&nbsp;&nbsp;&nbsp;&nbsp;: Personal Information shall not be provided to the Third Parties.</div>

            <div class="text-bold">5. Right to Refuse</div>
            <div class="mar-btm text-muted">&nbsp;&nbsp;&nbsp;&nbsp;: The External Customer has the right to refuse the collection and use of personal information, however, he or she shall be prohibited access to “${siteCompanyTitle}${isoTitle?has_content?then('-', '')}${isoTitle}” SOP/RF if refusing to provide its personal information.</div>

            <div class="text-bold mar-btm">6. ${siteCompanyTitle} shall not use the collected information without the consent of the External Customer for anything other than the original purpose aforementioned, and shall not provide it to the Third Parties.</div>

            <div class="mar-btm mar-top">I hereby confirm that I shall provide personal information (name, affiliation, e-mail and Role) for the access to “${siteCompanyTitle}${isoTitle?has_content?then('-', '')}${isoTitle}” SOP/RF</div>
<#--            <div class="mar-top mar-btm">-->
<#--                <label class="checkbox-inline">-->
<#--                    <input type="checkbox" name="agree" value="1"> I agree-->
<#--                </label>-->
<#--                <label class="checkbox-inline">-->
<#--                    <input type="checkbox" name="agree" value="0"> I do not agree-->
<#--                </label>-->
<#--            </div>-->
            <div>&nbsp;</div>
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
<#--                        <script>signatureCapture();</script>-->
<#--                        <button type="button" onclick="signatureSave()">Save signature</button>-->
<#--                        <button type="button" onclick="signatureClear()">Clear signature</button>-->
<#--                        </br>-->
<#--                        Saved Image-->
<#--                        </br>-->
<#--                        <img id="saveSignature" alt="Saved image png"/>-->
<#--                    </div>-->
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
