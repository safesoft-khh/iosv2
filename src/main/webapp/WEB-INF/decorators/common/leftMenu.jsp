<%--
  Created by IntelliJ IDEA.
  User: JHSEO
  Date: 2019-02-21
  Time: 오전 11:31
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sec:authentication property="principal.commaJobTitle" var="jobTitle"/>
<sec:authentication property="principal.training" var="training"/>
<sec:authentication property="principal" var="user"/>

<spring:eval expression="@environment.getProperty('site.iso-title')" var="isoTitle" />
<c:set var="isTraining" value="${training eq true and not empty jobTitle}"/>
<ul id="mainnav-menu" class="list-group" style="margin-top:10px !important;">

    <%-- Profile Area --%>
    <li style="display: none">
        <a href="#" aria-expanded="true">
            <i class="pli-user"></i>
            <span class="menu-title">User</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="true">
            <li><a href="/user/profile">Profile</a></li>
            <li><a href="/user/signature">Signature</a></li>
        </ul>
    </li>
    <li>
        <%-- Profile Area --%>
        <a href="#" aria-expanded="true">
            <i class="pli-home"></i>
            <span class="menu-title">Home</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="true">
            <li><a href="/notice">공지사항</a></li>
            <li><a href="/certifications">인증현황</a></li>
        </ul>
    </li>
    <li>
        <a href="/document" aria-expanded="false">
            <i class="pli-folder-binder"></i>
            <span class="menu-title">ISO</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <li><a href="/iso-14155">${isoTitle}</a></li>
        </ul>
    </li>
    <li>
        <a href="#" aria-expanded="false">
            <i class="pli-university"></i>
            <span class="menu-title">ISO Training</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <li><a href="/training/iso/mytraining">My Training</a></li>
            <li><a href="/training/iso/offline-training">Off-line Training</a></li>
            <li><a href="/training/iso/trainingLog">Training Log(ISO)</a></li>
            <c:if test="${user.teamManager == true || user.deptManager == true}">
                <li><a href="/training/iso/teamDeptTrainingLog2">Employee Training Log</a></li>
            </c:if>
        </ul>
    </li>
    <li>
        <a href="/document" aria-expanded="false">
            <i class="pli-folder-binder"></i>
            <span class="menu-title">SOP</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <li><a href="/sop/effective">Effective SOP</a></li>
            <li><a href="/sop/superseded">Superseded SOP</a></li>
            <li><a href="/sop/approved">Approved SOP</a></li>
        </ul>
    </li>
    <li>
        <a href="#" aria-expanded="false">
            <i class="pli-university"></i>
            <span class="menu-title">SOP Training</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <li><a href="/training/sop/my-training-matrix">My Training Matrix</a></li>
            <li><a href="/training/sop/mandatory-training">My Training</a></li>
            <li><a href="/training/sop/optional-training">Optional Training</a></li>
            <li><a href="/training/sop/offline-training">Off-line Training</a></li>
            <li><a href="/training/sop/trainingLog">Training Log(SOP)</a></li>
            <c:if test="${user.teamManager == true || user.deptManager == true}">
                <li><a href="/training/sop/teamDeptTrainingLog2">Employee Training Log</a></li>
            </c:if>
        </ul>
    </li>
    <li>
        <a href="/approval" aria-expanded="false">
            <i class="pli-check"></i>
            <span class="menu-title">전자결재</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <li>
                <a href="/approval/box/requester" aria-expanded="false">기안함</a>
            </li>
<%--            <li>--%>
<%--                <a href="/approval/box/reviewer" aria-expanded="false">검토함</a>--%>
<%--            </li>--%>
            <li>
                <a href="/approval/box/approver" aria-expanded="false">승인함</a>
            </li>
            </li>
        </ul>
    </li>

    <%--    <li>--%>
    <%--        <a href="xx#xxx" aria-expanded="false">--%>
    <%--            <i class="pli-box-with-folders"></i>--%>
    <%--            <span class="menu-title">Digital Binder</span>--%>
    <%--            <i class="arrow"></i>--%>
    <%--        </a>--%>
    <%--        <!--Submenu-->--%>
    <%--        <ul class="collapse" aria-expanded="false">--%>
    <%--            <li><a href="#">CV</a></li>--%>
    <%--            <li><a href="#">JD</a></li>--%>
    <%--            <li><a href="#">Employee Training Log(TM)</a></li>--%>
    <%--            <li><a href="#">Employee Training Log(SOP)</a></li>--%>
    <%--            <li><a href="#">Certificates</a></li>--%>
    <%--        </ul>--%>
    <%--    </li>--%>

</ul>