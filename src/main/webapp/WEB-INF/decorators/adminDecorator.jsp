<%--
  Created by IntelliJ IDEA.
  User: JHSEO
  Date: 2019-02-21
  Time: 오전 11:29
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <%--    <meta name="viewport" content="width=device-width, initial-scale=1.0">--%>
    <%--    <meta http-equiv="X-UA-Compatible" content="IE=edge">--%>
    <meta name="viewport" content="width=device-width,user-scalable=no,initial-scale=1, minimum-scale=1,maximum-scale=1"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>

    <title><sitemesh:write property='title'/></title>


    <jsp:include page="/WEB-INF/decorators/common/head.jsp?admin=true"/>
    <sitemesh:write property='head'/>

</head>

<!--TIPS-->
<!--You may remove all ID or Class names which contain "demo-", they are only used for demonstration. -->

<body>
<div id="container" class="aside-float aside-bright mainnav-lg print-content">
    <jsp:include page="/WEB-INF/decorators/common/adminTop.jsp"/>

    <div class="boxed">

        <!--CONTENT CONTAINER-->
        <!--===================================================-->
        <div id="content-container">
            <div id="page-head" class="hidden-print">

                <!--Page Title-->
                <!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
                <div id="page-title">
                    <h1 class="page-header text-overflow"></h1>
                </div>
                <!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
                <!--End page title-->


                <!--Breadcrumb-->
                <!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
                <ol class="breadcrumb" id="breadcrumb">
<%--                    <li><a href="#"><i class="pli-home"></i></a></li>--%>
                </ol>
                <!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
                <!--End breadcrumb-->


            </div>


            <!--Page content-->
            <!--===================================================-->
            <div id="page-content">


                <!-- QUICK TIPS -->
                <!-- ==================================================================== -->
                <div class="row">
                    <sitemesh:write property='body'/>
                </div>
                <!-- ==================================================================== -->
                <!-- END QUICK TIPS -->
            </div>
            <!--===================================================-->
            <!--End page content-->


        </div>
        <!--===================================================-->
        <!--END CONTENT CONTAINER-->


        <!--MAIN NAVIGATION-->
        <!--===================================================-->
        <nav id="mainnav-container">
            <div id="mainnav">


                <!--OPTIONAL : ADD YOUR LOGO TO THE NAVIGATION-->
                <!--It will only appear on small screen devices.-->
                <!--================================
                <div class="mainnav-brand">
                <a href="index.html" class="brand">
                <img src="/static/img/logo.png" alt="Nifty Logo" class="brand-icon">
                <span class="brand-text">Nifty</span>
                </a>
                <a href="#" class="mainnav-toggle"><i class="pci-cross pci-circle icon-lg"></i></a>
                </div>
                -->


                <!--Menu-->
                <!--================================-->
                <div id="mainnav-menu-wrap">
                    <div class="nano">
                        <div class="nano-content">

                            <!--Profile Widget-->
                            <!--================================-->
                            <jsp:include page="/WEB-INF/decorators/common/profile.jsp"/>
                            <!--END Profile Widget-->

                            <!--Shortcut buttons-->
                            <!--================================-->
                            <!--================================-->
                            <!--End shortcut buttons-->
                            <jsp:include page="/WEB-INF/decorators/common/adminLeftMenu.jsp"/>

                            <!--Widget-->
                            <!--================================-->
                            <!--================================-->
                            <!--End widget-->

                        </div>
                    </div>
                </div>
                <!--================================-->
                <!--End menu-->

            </div>
        </nav>
        <!--===================================================-->
        <!--END MAIN NAVIGATION-->

        <!--ASIDE-->
        <!--===================================================-->
        <%--        <jsp:include page="/WEB-INF/decorators/common/aside.jsp"/>--%>
        <!--===================================================-->
        <!--END ASIDE-->

    </div>


    <!-- FOOTER -->
    <!--===================================================-->
    <%--    <jsp:include page="/WEB-INF/decorators/common/footer.jsp"/>--%>
    <!--===================================================-->
    <!-- END FOOTER -->


    <!-- SCROLL PAGE BUTTON -->
    <!--===================================================-->
    <button class="scroll-top btn">
        <i class="pci-chevron chevron-up"></i>
    </button>
    <!--===================================================-->
</div>
<!--===================================================-->
<!-- END OF CONTAINER -->


</body>
</html>
