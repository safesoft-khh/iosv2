<#import "common/customSpring.ftl" as spring />
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>ISO Management System > Sign Up</title>

    <!--Premium Icons [ OPTIONAL ]-->
    <link href="/static/premium/icon-sets/icons/line-icons/premium-line-icons.min.css" rel="stylesheet">
    <!-- Font Icon -->
    <link rel="stylesheet" href="/static/material-icon/css/material-design-iconic-font.min.css" type="text/css">

    <!--Bootstrap Stylesheet [ REQUIRED ]-->
    <link href="/static/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap DatePicker -->
    <link href="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.css" rel="stylesheet">
    <!-- select2 -->
    <link href="/static/plugins/select2/css/select2.min.css" rel="stylesheet">

    <!-- Main css -->
    <#--    <link rel="stylesheet" href="http://lms.dtnsm.com/default/login/css/style.css">-->
    <!-- JS -->
    <script src="/static/js/jquery.min.js"></script>
    <!--BootstrapJS [ RECOMMENDED ]-->
    <script src="/static/js/bootstrap.min.js"></script>
    <!--Bootstrap Validator -->
    <script src="/static/plugins/bootstrap-validator/bootstrapValidator.js"></script>
    <!-- Bootstrap DatePicker -->
    <script src="/static/plugins/bootstrap-datepicker/bootstrap-datepicker.min.js"></script>
    <!-- select2 -->
    <script src="/static/plugins/select2/js/select2.min.js"></script>

    <style>
        .form-group {
            margin-bottom: 10px;
        }
    </style>

    <script>
        $(document).ready(function () {

            //SignUp Validation Setting
            signUpValidation();

            $("#birthDate").datepicker({
                format: "yyyy-mm-dd",
                todayBtn: "linked",
                endDate: new Date(),
                clearBtn: true,
                autoclose: true,
                todayHighlight: true,
            }).on('hide.bs.modal', function (e) {
                e.stopPropagation(); //datepicker가 close될 때, sign-up-modal까지의 hide.bs.modal Event의 전파를 막음.
            });

            $("#indate").datepicker({
                format: "yyyy-mm-dd",
                todayBtn: "linked",
                endDate: new Date(),
                clearBtn: true,
                autoclose: true,
                todayHighlight: true,
            }).on('hide.bs.modal', function (e) {
                e.stopPropagation(); //datepicker가 close될 때, sign-up-modal까지의 hide.bs.modal Event의 전파를 막음.
            });



            //JD select2 Setting
            $('#jdIds').select2({width : '100%'});

            //초기 화면 전환시 세팅
            departmentSelect($("#deptSelect").val());

            $("#deptSelect").change(function(){
                departmentSelect($(this).val());
            })

            $("#teamSelect").change(function(){
                let res = $(this).val();

                if(res == 'department') {
                    $("#department").val($("#deptSelect").val());
                }else {
                    $("#department").val(res);
                }
            })
        })


        function departmentSelect(value) {
            $("#department").val(value);

            $.ajax({
                url: "/signUp/ajax/department",
                method: 'post',
                data: {id: value},
                success: function (data) {
                    let teamSelect = $("#teamSelect");

                    if(data != '') {
                        teamSelect.attr("disabled", false);
                        teamSelect.html(data);
                    } else {
                        teamSelect.attr("disabled", true);
                        teamSelect.html(data);
                    }
                }
            });
        }

        function signUpValidation() {
            //Sign Up Setup
            $("#signup-form").bootstrapValidator({
                message: 'This value is not valid',
                feedbackIcons: {
                    valid: 'pli-yes text-success',
                    invalid: 'pli-close',
                    validating: 'pli-refresh'
                },
                fields: {
                    username: {
                        validators: {
                            remote: {
                                message: '> The id is already used',
                                url: '/signUp/ajax/validation',
                                type: 'POST',
                                data: {
                                    type: "username",
                                },
                                name: 'keyword' // 입력된 데이터 name을 keyword로 한다.
                            },
                            regexp: {
                                regexp: /^[a-zA-Z\d*]{5,20}$/,
                                message: '> The id length must 5 - 20 (alphabetical, number)'
                            },
                            notEmpty: {
                                message: '> The id is required'
                            }
                        }
                    },
                    name: {
                        validators: {
                            notEmpty: {
                                message: '> The name is required'
                            }
                        }
                    },
                    // engName: {
                    //     message: '> The engName is not valid',
                    //     validators: {
                    //         notEmpty: {
                    //             message: '> The engName is required'
                    //         }
                    //     }
                    // },
                    password: {
                        validators: {
                            notEmpty: {
                                message: '> The password is required and can\'t be empty'
                            },
                            identical: {
                                field: 'confirmPassword',
                                message: '> The password and its P/W Confirm are not the same'
                            },
                            regexp: {
                                //최소 8자, 대문자 하나 이상, 소문자 하나, 숫자 하나 및 특수문자 하나 이상
                                // regexp: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@$!%*?&])[A-Za-z\d$@$!%*?&]{8,}/,
                                //최소 8자, 최소 하나의 문자, 숫자, 특수문자 조합
                                regexp: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{8,}$/,
                                message: '> The password can over than 8 length consist of alphabetical, number, dot and special character'
                            }
                        }
                    },
                    confirmPassword: {
                        validators: {
                            notEmpty: {
                                message: '> The P/W Confirm is required and can\'t be empty'
                            },
                            identical: {
                                field: 'password',
                                message: '> The password and its P/W Confirm are not the same'
                            },
                            regexp: {
                                //최소 8자, 대문자 하나 이상, 소문자 하나, 숫자 하나 및 특수문자 하나 이상
                                //최소 8자, 최소 하나의 문자, 숫자, 특수문자 조합
                                regexp: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{8,}$/,
                                message: '> The password can over than 8 length consist of upper/lower alphabetical, number, dot and special character'
                            }
                        }
                    },
                    // birthDate: {
                    //     message: '> The birthDate is not valid',
                    //     validators: {
                    //         notEmpty: {
                    //             message: '> The birthDate is required'
                    //         }
                    //     }
                    // },
                    // indate: {
                    //     message: '> The Emplyoee Date is not valid',
                    //     validators: {
                    //         notEmpty: {
                    //             message: '> The Emplyoee Date is required'
                    //         }
                    //     }
                    // },
                    email: {
                        validators: {
                            remote: {
                                message: '> The email is already used',
                                url: '/signUp/ajax/validation',
                                type: 'POST',
                                data: {
                                    type: "email",
                                },
                                name: 'keyword' // 입력된 데이터 name을 keyword로 한다.
                            },
                            notEmpty: {
                                message: '> The email is required and can\'t be empty'
                            },
                            emailAddress: {
                                message: '> Please enter a valid email address'
                            }
                        }
                    },
                    phone: {
                        validators: {
                            regexp: {
                                regexp: /^[0-9]{3}[-]+[0-9]{4}[-]+[0-9]{4}$/,
                                message: '> The phone number is not valid (000-0000-0000)'
                            }
                        }
                    },
                    jdIds: {
                        validators: {
                            notEmpty: {
                                message: '> The ROLE is required and can\'t be empty'
                            }
                        }
                    }
                }
            }).on('success.field.bv', function (e, data) {
                // $(e.target)  --> The field element
                // data.bv      --> The BootstrapValidator instance
                // data.field   --> The field name
                // data.element --> The field element
                var $parent = data.element.parents('.form-group');

                console.log($parent);
                // Remove the has-success class
                $parent.removeClass('has-success');
            });
        }
    </script>
</head>
<body class="main" style="background-color: white">
<div class="cls-content">
    <!-- Sign Up Page -->
    <div class="panel panel-bordered-success cls-content-lg text-lg-left pad-top" style="width: 800px">
        <!--Modal header-->
        <div class="text-center mar-btm">
            <div class="row">
                <div class="col-xs-4"><img src="${imageLogo}"></div>
                <div class="col-xs-8 text-right pad-top"><span class="text-bold pad-rgt" style="font-size:24px;">ISO Management System > Sign Up</span></div>
            </div>
        </div>
        <hr>
        <form action="/signUp" method="POST" class="register-form" id="signup-form">
            <!--Modal body-->
            <div class="panel-body">
                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="control-label" for="username">ID* : </label>
                            <@spring.formInput "account.username" "class='form-control'" />
                            <@spring.showErrors "" "color:#a94442; font-wieght: 700;"/>
                        </div>
                    </div>

                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="control-label" for="email">E-mail* : </label>
                            <@spring.formInput "account.email" "class='form-control'" "email"/>
                            <@spring.showErrors "" "color:#a94442; font-wieght: 700;"/>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="control-label" for="password">Password* : </label>
                            <@spring.formInput "account.password" "class='form-control'" "password" />
                            <@spring.showErrors "" "color:#a94442; font-wieght: 700;"/>
                        </div>
                    </div>

                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="control-label" for="confirmPassword">P/W Confirm* : </label>
                            <input class="form-control" type="password" name="confirmPassword" id="confirmPassword">
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="control-label" for="name">Name* : </label>
                            <@spring.formInput "account.name" "class='form-control'" />
                            <@spring.showErrors "" "color:#a94442; font-wieght: 700;"/>
                        </div>
                    </div>

                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="control-label" for="engName">Name(ENG) : </label>
                            <@spring.formInput "account.engName" "class='form-control'" />
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="control-label" for="birthDate">Birth Date* : </label>
                            <@spring.formInput "account.birthDate" "class='form-control'" />
                            <@spring.showErrors "" "color:#a94442; font-wieght: 700;"/>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="control-label" for="phone">Phone : </label>
                            <@spring.formInput "account.phone" "class='form-control'" />
                            <@spring.showErrors "" "color:#a94442; font-wieght: 700;"/>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <@spring.formHiddenInput "account.department" "class='form-control'"/>
                    <div class="col-sm-6">
                        <label class="control-label">Department / Team* : </label>
                        <div class="form-group">
                            <div class="col-sm-6" style="padding-left: 0px">
                                <select id="deptSelect" class="form-control">
                                    <#list departments  as d>
                                        <option value="${d.id}">${d.name}</option>
                                    </#list>
                                </select>
                            </div>
                            <div class="col-sm-6" style="padding-right: 0px">
                                <select id="teamSelect" class="form-control" disabled></select>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="control-label">ROLE* : </label>
                            <@spring.formMultiSelect "account.jdIds" jobDescriptionMap "class='form-control'"/>
                            <@spring.showErrors "" "color:#a94442; font-wieght: 700;"/>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="control-label" for="indate">Employee Date* : </label>
                            <@spring.formInput "account.indate" "class='form-control'" />
                            <@spring.showErrors "" "color:#a94442; font-wieght: 700;"/>
                        </div>
                    </div>
                </div>

            </div>
            <!--Modal footer-->
            <div class="panel-footer text-right">
                <button class="btn btn-default" id="requestBtn" type="submit"><i class="pli-yes"></i> Request</button>
                <a data-dismiss="modal" class="btn btn-default" href="/login"><i class="pli-back"></i> Back</a>
            </div>
        </form>
    </div>
    <!-- Sign Up Page -->
</div>
</body>
</html>