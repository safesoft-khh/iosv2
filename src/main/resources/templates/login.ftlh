<!DOCTYPE html>

<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <meta name="description" content="${description}" />
    <meta name="keywords" content="${keywords}" />

    <title>ISO Management System</title>

    <!--Premium Icons [ OPTIONAL ]-->
    <link href="/static/premium/icon-sets/icons/line-icons/premium-line-icons.min.css" rel="stylesheet">
    <!-- Font Icon -->
    <link rel="stylesheet" href="/static/material-icon/css/material-design-iconic-font.min.css" type="text/css">

    <!--Bootstrap Stylesheet [ REQUIRED ]-->
    <link href="/static/css/bootstrap.min.css" rel="stylesheet">
    <!-- Main css -->
    <#--    <link rel="stylesheet" href="http://lms.dtnsm.com/default/login/css/style.css">-->
    <!-- JS -->
    <script src="/static/js/jquery.min.js"></script>
    <!--BootstrapJS [ RECOMMENDED ]-->
    <script src="/static/js/bootstrap.min.js"></script>

    <style>
        h1 {
            font-size: 2em;
            text-align: justify;
        }

        .title-head {
            /*font-size: 35px;*/
            color: #2783D8;
        }

        /* @extend display-flex; */
        display-flex, .display-flex, .display-flex-center, .signup-content, .signin-content, .social-login, .socials {
            display: flex;
            display: -webkit-flex;
        }

        /* @extend list-type-ulli; */
        list-type-ulli, .socials {
            list-style-type: none;
            margin: 0;
            padding: 0;
        }

        /* poppins-300 - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: normal;
            font-weight: 300;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-300.eot");
            /* IE9 Compat Modes */
            src: local("Poppins Light"), local("Poppins-Light"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-300.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-300.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-300.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-300.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-300.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-300italic - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: italic;
            font-weight: 300;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-300italic.eot");
            /* IE9 Compat Modes */
            src: local("Poppins Light Italic"), local("Poppins-LightItalic"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-300italic.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-300italic.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-300italic.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-300italic.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-300italic.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-regular - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: normal;
            font-weight: 400;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-regular.eot");
            /* IE9 Compat Modes */
            src: local("Poppins Regular"), local("Poppins-Regular"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-regular.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-regular.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-regular.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-regular.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-regular.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-italic - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: italic;
            font-weight: 400;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-italic.eot");
            /* IE9 Compat Modes */
            src: local("Poppins Italic"), local("Poppins-Italic"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-italic.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-italic.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-italic.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-italic.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-italic.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-500 - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: normal;
            font-weight: 500;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-500.eot");
            /* IE9 Compat Modes */
            src: local("Poppins Medium"), local("Poppins-Medium"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-500.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-500.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-500.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-500.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-500.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-500italic - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: italic;
            font-weight: 500;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-500italic.eot");
            /* IE9 Compat Modes */
            src: local("Poppins Medium Italic"), local("Poppins-MediumItalic"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-500italic.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-500italic.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-500italic.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-500italic.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-500italic.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-600 - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: normal;
            font-weight: 600;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-600.eot");
            /* IE9 Compat Modes */
            src: local("Poppins SemiBold"), local("Poppins-SemiBold"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-600.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-600.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-600.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-600.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-600.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-700 - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: normal;
            font-weight: 700;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-700.eot");
            /* IE9 Compat Modes */
            src: local("Poppins Bold"), local("Poppins-Bold"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-700.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-700.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-700.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-700.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-700.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-700italic - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: italic;
            font-weight: 700;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-700italic.eot");
            /* IE9 Compat Modes */
            src: local("Poppins Bold Italic"), local("Poppins-BoldItalic"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-700italic.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-700italic.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-700italic.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-700italic.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-700italic.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-800 - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: normal;
            font-weight: 800;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-800.eot");
            /* IE9 Compat Modes */
            src: local("Poppins ExtraBold"), local("Poppins-ExtraBold"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-800.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-800.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-800.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-800.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-800.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-800italic - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: italic;
            font-weight: 800;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-800italic.eot");
            /* IE9 Compat Modes */
            src: local("Poppins ExtraBold Italic"), local("Poppins-ExtraBoldItalic"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-800italic.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-800italic.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-800italic.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-800italic.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-800italic.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        /* poppins-900 - latin */
        @font-face {
            font-family: 'Poppins';
            font-style: normal;
            font-weight: 900;
            src: url("/static/material-icon/fonts/poppins/poppins-v5-latin-900.eot");
            /* IE9 Compat Modes */
            src: local("Poppins Black"), local("Poppins-Black"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-900.eot?#iefix") format("embedded-opentype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-900.woff2") format("woff2"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-900.woff") format("woff"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-900.ttf") format("truetype"), url("/static/material-icon/fonts/poppins/poppins-v5-latin-900.svg#Poppins") format("svg");
            /* Legacy iOS */
        }

        a:focus, a:active {
            text-decoration: none;
            outline: none;
            transition: all 300ms ease 0s;
            -moz-transition: all 300ms ease 0s;
            -webkit-transition: all 300ms ease 0s;
            -o-transition: all 300ms ease 0s;
            -ms-transition: all 300ms ease 0s;
        }

        input, select, textarea {
            outline: none;
            appearance: unset !important;
            -moz-appearance: unset !important;
            -webkit-appearance: unset !important;
            -o-appearance: unset !important;
            -ms-appearance: unset !important;
        }

        input::-webkit-outer-spin-button, input::-webkit-inner-spin-button {
            appearance: none !important;
            -moz-appearance: none !important;
            -webkit-appearance: none !important;
            -o-appearance: none !important;
            -ms-appearance: none !important;
            margin: 0;
        }

        input:focus, select:focus, textarea:focus {
            outline: none;
            box-shadow: none !important;
            -moz-box-shadow: none !important;
            -webkit-box-shadow: none !important;
            -o-box-shadow: none !important;
            -ms-box-shadow: none !important;
        }

        input[type=checkbox] {
            appearance: checkbox !important;
            -moz-appearance: checkbox !important;
            -webkit-appearance: checkbox !important;
            -o-appearance: checkbox !important;
            -ms-appearance: checkbox !important;
        }

        input[type=radio] {
            appearance: radio !important;
            -moz-appearance: radio !important;
            -webkit-appearance: radio !important;
            -o-appearance: radio !important;
            -ms-appearance: radio !important;
        }

        img {
            max-width: 100%;
            height: auto;
        }

        figure {
            margin: 0;
        }

        p {
            margin-bottom: 0px;
            font-size: 15px;
            color: #777;
        }

        h2 {
            /*line-height: 1.66;*/
            margin: 0;
            padding: 0;
            font-weight: bold;
            color: #222;
            font-family: Poppins;
            font-size: 30px;
            /*font-size: 36px; */
        }

        .main {
            background: #f8f8f8;
            /*background-image: linear-gradient(rgba(0,0,0,0.35), rgba(0,0,0,0.35)),*/
            /*                  url("/static/img/login_logo.jpg");*/
            /*background-size: 165%;*/

            padding: 150px 0;
        }

        .clear {
            clear: both;
        }

        body {
            font-size: 13px;
            line-height: 1.8;
            color: #222;
            background: #f8f8f8;
            font-weight: 400;
            font-family: Poppins;
        }

        .container {
            min-width: 900px;
            background: #fff;
            margin: 0 auto;
            border: 1px solid RGBA(232, 232, 232, 0.6);
            box-shadow: 0px 15px 16.83px 0.17px rgba(0, 0, 0, 0.05);
            -moz-box-shadow: 0px 15px 16.83px 0.17px rgba(0, 0, 0, 0.05);
            -webkit-box-shadow: 0px 15px 16.83px 0.17px rgba(0, 0, 0, 0.05);
            -o-box-shadow: 0px 15px 16.83px 0.17px rgba(0, 0, 0, 0.05);
            -ms-box-shadow: 0px 15px 16.83px 0.17px rgba(0, 0, 0, 0.05);
            border-radius: 20px;
            -moz-border-radius: 20px;
            -webkit-border-radius: 20px;
            -o-border-radius: 20px;
            -ms-border-radius: 20px;
        }

        .display-flex {
            justify-content: space-between;
            -moz-justify-content: space-between;
            -webkit-justify-content: space-between;
            -o-justify-content: space-between;
            -ms-justify-content: space-between;
            align-items: center;
            -moz-align-items: center;
            -webkit-align-items: center;
            -o-align-items: center;
            -ms-align-items: center;
        }

        .display-flex-center {
            justify-content: center;
            -moz-justify-content: center;
            -webkit-justify-content: center;
            -o-justify-content: center;
            -ms-justify-content: center;
            align-items: center;
            -moz-align-items: center;
            -webkit-align-items: center;
            -o-align-items: center;
            -ms-align-items: center;
        }

        .position-center {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            -moz-transform: translate(-50%, -50%);
            -webkit-transform: translate(-50%, -50%);
            -o-transform: translate(-50%, -50%);
            -ms-transform: translate(-50%, -50%);
        }


        .signup {
            margin-bottom: 150px;
        }

        .signup-content {
            padding: 50px 0;
        }

        .signup-form, .signup-image, .signin-form, .signin-image {
            width: 60%;
            overflow: hidden;
        }

        .signup-image {
            display: flex;
            justify-content: center;
            align-items: center;
            margin-right: 50px;
        }

        .signup-form {
            width: 40%;
            overflow: hidden;
        }

        .form-title {
            margin-bottom: 33px;
        }

        figure {
            margin-bottom: 50px;
            text-align: center;
        }

        .form-submit {
            display: inline-block;
            background: #6dabe4;
            color: #fff;
            border-bottom: none;
            width: auto;
            padding: 15px 39px;
            border-radius: 5px;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            -o-border-radius: 5px;
            -ms-border-radius: 5px;
            margin-top: 25px;
            cursor: pointer;
        }

        .form-submit:hover {
            background: #4292dc;
        }

        #signIn {
            margin-top: 16px;
            float:left;
        }

        #signUp{
            margin-top: 16px;
            float:right;
        }

        .signup-image-link {
            font-size: 14px;
            color: #222;
            display: block;
            text-align: center;
        }

        .term-service {
            font-size: 13px;
            color: #222;
        }

        .signup-form {
            margin-left: 75px;
            margin-right: 75px;
            padding-left: 34px;
        }

        #signup-form i {
            margin-right: 15px;
        }

        .register-form {
            width: 100%;
        }

        .form-group {
            position: relative;
            margin-bottom: 25px;
            overflow: hidden;
        }

        .form-group:last-child {
            margin-bottom: 0px;
        }

        input {
            width: 100%;
            display: block;
            border: none;
            border-bottom: 1px solid #999;
            padding: 6px 30px;
            font-family: Poppins;
            box-sizing: border-box;
        }

        input::-webkit-input-placeholder {
            color: #999;
        }

        input::-moz-placeholder {
            color: #999;
        }

        input:-ms-input-placeholder {
            color: #999;
        }

        input:-moz-placeholder {
            color: #999;
        }

        input:focus {
            border-bottom: 1px solid #222;
        }

        input:focus::-webkit-input-placeholder {
            color: #222;
        }

        input:focus::-moz-placeholder {
            color: #222;
        }

        input:focus:-ms-input-placeholder {
            color: #222;
        }

        input:focus:-moz-placeholder {
            color: #222;
        }

        input[type=checkbox]:not(old) {
            width: 2em;
            margin: 0;
            padding: 0;
            font-size: 1em;
            display: none;
        }

        input[type=checkbox]:not(old) + label {
            display: inline-block;
            line-height: 1.5em;
            margin-top: 6px;
        }

        input[type=checkbox]:not(old) + label > span {
            display: inline-block;
            width: 13px;
            height: 13px;
            margin-right: 15px;
            margin-bottom: 3px;
            border: 1px solid #999;
            border-radius: 2px;
            -moz-border-radius: 2px;
            -webkit-border-radius: 2px;
            -o-border-radius: 2px;
            -ms-border-radius: 2px;
            background: white;
            background-image: -moz-linear-gradient(white, white);
            background-image: -ms-linear-gradient(white, white);
            background-image: -o-linear-gradient(white, white);
            background-image: -webkit-linear-gradient(white, white);
            background-image: linear-gradient(white, white);
            vertical-align: bottom;
        }

        input[type=checkbox]:not(old):checked + label > span {
            background-image: -moz-linear-gradient(white, white);
            background-image: -ms-linear-gradient(white, white);
            background-image: -o-linear-gradient(white, white);
            background-image: -webkit-linear-gradient(white, white);
            background-image: linear-gradient(white, white);
        }

        input[type=checkbox]:not(old):checked + label > span:before {
            content: '\f26b';
            display: block;
            color: #222;
            font-size: 11px;
            line-height: 1.2;
            text-align: center;
            font-family: 'Material-Design-Iconic-Font';
            font-weight: bold;
        }

        .agree-term {
            display: inline-block;
            width: auto;
        }

        label {
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            -moz-transform: translateY(-50%);
            -webkit-transform: translateY(-50%);
            -o-transform: translateY(-50%);
            -ms-transform: translateY(-50%);
            color: #222;
        }

        label.signup-label {
            transform: translateY(0%);
            -moz-transform: translateY(0%);
            -webkit-transform: translateY(0%);
            -o-transform: translateY(0%);
            -ms-transform: translateY(0%);
        }

        .label-has-error {
            top: 22%;
        }

        label.error {
            position: relative;
            background: url("../images/unchecked.gif") no-repeat;
            background-position-y: 3px;
            padding-left: 20px;
            display: block;
            margin-top: 20px;
        }

        label.valid {
            display: block;
            position: absolute;
            right: 0;
            left: auto;
            margin-top: -6px;
            width: 20px;
            height: 20px;
            background: transparent;
        }

        label.valid:after {
            font-family: 'Material-Design-Iconic-Font';
            content: '\f269';
            width: 100%;
            height: 100%;
            position: absolute;
            /* right: 0; */
            font-size: 16px;
            color: green;
        }

        .label-agree-term {
            position: relative;
            top: 0%;
            transform: translateY(0);
            -moz-transform: translateY(0);
            -webkit-transform: translateY(0);
            -o-transform: translateY(0);
            -ms-transform: translateY(0);
        }

        .material-icons-name {
            font-size: 18px;
        }

        .signin-content {
            padding-top: 67px;
            padding-bottom: 87px;
        }

        .social-login {
            align-items: center;
            -moz-align-items: center;
            -webkit-align-items: center;
            -o-align-items: center;
            -ms-align-items: center;
            margin-top: 80px;
        }

        .social-label {
            display: inline-block;
            margin-right: 15px;
        }

        .socials li {
            padding: 5px;
        }

        .socials li:last-child {
            margin-right: 0px;
        }

        .socials li a {
            text-decoration: none;
        }

        .socials li a i {
            width: 30px;
            height: 30px;
            color: #fff;
            font-size: 14px;
            border-radius: 5px;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            -o-border-radius: 5px;
            -ms-border-radius: 5px;
            transform: translateZ(0);
            -moz-transform: translateZ(0);
            -webkit-transform: translateZ(0);
            -o-transform: translateZ(0);
            -ms-transform: translateZ(0);
            -webkit-transition-duration: 0.3s;
            transition-duration: 0.3s;
            -webkit-transition-property: transform;
            transition-property: transform;
            -webkit-transition-timing-function: ease-out;
            transition-timing-function: ease-out;
        }

        .socials li:hover a i {
            -webkit-transform: scale(1.3) translateZ(0);
            transform: scale(1.3) translateZ(0);
        }

        .zmdi-facebook {
            background: #3b5998;
        }

        .zmdi-twitter {
            background: #1da0f2;
        }

        .zmdi-google {
            background: #e72734;
        }

        .signin-form {
            margin-right: 90px;
            margin-left: 80px;
        }

        .signin-image {
            margin-left: 110px;
            margin-right: 20px;
            margin-top: 10px;
        }

        @media screen and (max-width: 1200px) {
            .container {
                width: calc(100% - 30px);
                max-width: 100%;
            }
        }

        @media screen and (min-width: 1024px) {
            .container {
                max-width: 1250px;
            }
        }

        @media screen and (max-width: 768px) {
            .signup-content, .signin-content {
                flex-direction: column;
                -moz-flex-direction: column;
                -webkit-flex-direction: column;
                -o-flex-direction: column;
                -ms-flex-direction: column;
                justify-content: center;
                -moz-justify-content: center;
                -webkit-justify-content: center;
                -o-justify-content: center;
                -ms-justify-content: center;
            }

            .signup-form {
                margin-left: 0px;
                margin-right: 0px;
                padding-left: 0px;
                /* box-sizing: border-box; */
                padding: 0 30px;
            }

            .signin-image {
                margin-left: 0px;
                margin-right: 0px;
                margin-top: 50px;
                order: 2;
                -moz-order: 2;
                -webkit-order: 2;
                -o-order: 2;
                -ms-order: 2;
            }

            .signup-form, .signup-image, .signin-form, .signin-image {
                width: auto;
            }

            .social-login {
                justify-content: center;
                -moz-justify-content: center;
                -webkit-justify-content: center;
                -o-justify-content: center;
                -ms-justify-content: center;
            }

            .form-button {
                text-align: center;
            }

            .signin-form {
                order: 1;
                -moz-order: 1;
                -webkit-order: 1;
                -o-order: 1;
                -ms-order: 1;
                margin-right: 0px;
                margin-left: 0px;
                padding: 0 30px;
            }

            .form-title {
                text-align: center;
            }
        }

        @media screen and (max-width: 400px) {
            .social-login {
                flex-direction: column;
                -moz-flex-direction: column;
                -webkit-flex-direction: column;
                -o-flex-direction: column;
                -ms-flex-direction: column;
            }

            .social-label {
                margin-right: 0px;
                margin-bottom: 10px;
            }
        }

        /*# sourceMappingURL=style.css.map */
    </style>

    <script type="text/javascript">
        $(document).ready(function () {
            // 저장된 쿠키값을 가져와서 ID 칸에 넣어준다. 없으면 공백으로 들어감.
            var userInputId = getCookie("userInputId");
            $("#register-form input[name='username']").val(userInputId);

            $("#register-form #username").keyup(function () {
                if ($(this).val().indexOf('@') != -1) {
                    $("#div_pwd").hide();
                } else {
                    $("#div_pwd").show();
                }
            });

            if ($("#register-form #username").val()) {
                $("#register-form #username").trigger("keyup");
            }

            if ($("#register-form input[name='username']").val() != "") { // 그 전에 ID를 저장해서 처음 페이지 로딩 시, 입력 칸에 저장된 ID가 표시된 상태라면,
                $("#form-checkbox").attr("checked", true); // ID 저장하기를 체크 상태로 두기.
            }

            $("#form-checkbox").change(function () { // 체크박스에 변화가 있다면,
                if ($("#form-checkbox").is(":checked")) { // ID 저장하기 체크했을 때,
                    var userInputId = $("input[name='username']").val();
                    setCookie("userInputId", userInputId, 7); // 7일 동안 쿠키 보관
                } else { // ID 저장하기 체크 해제 시,
                    deleteCookie("userInputId");
                }
            });

            // ID 저장하기를 체크한 상태에서 ID를 입력하는 경우, 이럴 때도 쿠키 저장.
            $("#register-form input[name='username']").keyup(function () { // ID 입력 칸에 ID를 입력할 때,
                if ($("#form-checkbox").is(":checked")) { // ID 저장하기를 체크한 상태라면,
                    var userInputId = $("input[name='username']").val();
                    setCookie("userInputId", userInputId, 7); // 7일 동안 쿠키 보관
                }
            });

            $('#register-form #username').focus();

        });

        function setCookie(cookieName, value, exdays) {
            var exdate = new Date();
            exdate.setDate(exdate.getDate() + exdays);
            var cookieValue = escape(value) + ((exdays == null) ? "" : "; expires=" + exdate.toGMTString());
            document.cookie = cookieName + "=" + cookieValue;
        }

        function deleteCookie(cookieName) {
            var expireDate = new Date();
            expireDate.setDate(expireDate.getDate() - 1);
            document.cookie = cookieName + "= " + "; expires=" + expireDate.toGMTString();
        }

        function getCookie(cookieName) {
            cookieName = cookieName + '=';
            var cookieData = document.cookie;
            var start = cookieData.indexOf(cookieName);
            var cookieValue = '';
            if (start != -1) {
                start += cookieName.length;
                var end = cookieData.indexOf(';', start);
                if (end == -1) end = cookieData.length;
                cookieValue = cookieData.substring(start, end);
            }
            return unescape(cookieValue);
        }


    </script>
</head>
<body>
<div class="main" style="background-color: white">
    <!--        <div class="main" style="background-color: RGBA(109,171,228, 0.47)">    -->
    <!-- Sign up form -->
    <section class="signup">
        <div class="container">
            <div class="signup-content">
                <div class="signup-form" style="padding: 0">
                    <a href="${siteLink}" target="_blank">
                        <#-- TODO:// Image Logo 변경 -->
                        <img src="${imageLogo}" style="height: 40px; padding-bottom: 10px" alt="image"></a>
                    <h2 class="form-title" style="padding-bottom: 5px">
                        <span class="title-head" style="padding-top: 5px;">ISO</span>
                        <span class="title-head">M</span>anagement
                        <span class="title-head">S</span>ystem
                    </h2>
                    <form action="/login" method="POST" class="register-form" id="register-form" style="padding: 0">
                        <#if RequestParameters.sessionExpired?exists || RequestParameters.invalidSession?exists>
                            <div class="alert alert-danger">
                                Your session has expired.
                            </div>
                        <#elseif RequestParameters.passwordReset?exists>
                            <div class="alert alert-info">
                                Temporary password was sent to input email. please check.
                            </div>
                        <#elseif RequestParameters.error?exists>
                            <#if RequestParameters.error=='locked'>
                                <div class="alert alert-danger">
                                    This account was locked.
                                </div>
                            <#elseif RequestParameters.error=='disabled'>
                                <div class="alert alert-danger">
                                    This account was disabled.
                                </div>
                            <#elseif RequestParameters.error=='accountExpired'>
                                <div class="alert alert-danger">
                                    This account was expired
                                </div>
                            <#elseif RequestParameters.error=='credentialExpired'>
                                <div class="alert alert-danger">
                                    This account's credential was expired
                                </div>
                            <#-- UsernameNotFound & BadCredentials -->
                            <#elseif RequestParameters.error=='signup'>
                                <div class="alert alert-warning">
                                    This account is in the process of sign up requested
                                </div>
                            <#else>
                                <div class="alert alert-danger">
                                    Invalid username and password.
                                </div>
                            </#if>
                        <#elseif RequestParameters.logout?exists>
                            <div class="alert alert-success">
                                You have been logged out.
                            </div>
                        <#elseif message?exists && messageType?exists>
                            <div class="alert alert-${messageType}">
                                ${message}
                            </div>
                        </#if>

                        <div class="form-group">
                            <label for="username"><i class="zmdi zmdi-account material-icons-name"></i></label>
                            <input type="text" name="username" id="username" placeholder="User ID"/>
                        </div>
                        <!--                            <div class="form-group">-->
                        <!--                                <label for="email"><i class="zmdi zmdi-email"></i></label>-->
                        <!--                                <input type="email" name="email" id="email" placeholder="Your Email"/>-->
                        <!--                            </div>-->
                        <div class="form-group" id="div_pwd">
                            <label for="password"><i class="zmdi zmdi-lock"></i></label>
                            <input type="password" name="password" id="password" placeholder="Password"/>
                        </div>
                        <!--                            <div class="form-group">-->
                        <!--                                <label for="re-pass"><i class="zmdi zmdi-lock-outline"></i></label>-->
                        <!--                                <input type="password" name="re_pass" id="re_pass" placeholder="Repeat your password"/>-->
                        <!--                            </div>-->
                        <div class="form-group">
                            <div class="row">
                                <div class="col-lg-6 text-left">
                                    <input type="checkbox" name="agree-term" id="form-checkbox" class="agree-term"/>
                                    <label for="form-checkbox" class="label-agree-term"><span><span></span></span>Saved ID</label>
                                </div>
                                <div class="col-lg-6 text-right">
                                    <a href="/forgot-password">Forgot Password?</a>
                                </div>
                            </div>
                        </div>
                        <div class="form-group form-button">
                            <input type="submit" name="signIn" id="signIn" class="form-submit" value="Login"/>
                            <input type="button" id="signUp" onclick="location.href='/signUp'" class="form-submit" value="Sign Up" />
                        </div>
                    </form>
                </div>
                <div class="signup-image">
                    <img src="${loginImage}" alt="sing up image">
                </div>
            </div>
        </div>
    </section>
</div>
</body><!-- This templates was made by Colorlib (https://colorlib.com) -->
</html>