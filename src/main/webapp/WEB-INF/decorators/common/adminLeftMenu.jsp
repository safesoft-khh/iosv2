<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:eval expression="@environment.getProperty('form.name')" var="formName" />

<ul id="mainnav-menu" class="list-group" style="margin-top:10px !important;">

    <li>
        <a href="/admin/dashboard" aria-expanded="false">
            <i class="pli-dashboard"></i>
            <span class="menu-title">Dashboard</span>
        </a>
    </li>
    <li>
        <a href="#" aria-expanded="false">
            <i class="pli-folder-with-document"></i>
            <span class="menu-title">SOP Management</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <%--            <li><a href="/admin/SOP/management/development">Development</a></li>--%>
            <%--            <li><a href="/admin/SOP/management/revision">Revision</a></li>--%>
            <li><a href="/admin/SOP/management/approved">Approved SOP</a></li>
            <li><a href="/admin/SOP/management/effective">Effective SOP</a></li>
            <li><a href="/admin/SOP/management/superseded">Superseded SOP</a></li>
<%--            <li><a href="/admin/SOP/management/retirement">Retirement SOP</a></li>--%>
            <li><a href="/admin/sop/category">SOP Category</a></li>
        </ul>
    </li>
    <li>
        <a href="#" aria-expanded="false">
            <i class="pli-file"></i>
            <span class="menu-title">${formName} Management</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <%--            <li><a href="/admin/RD/management/development">Development</a></li>--%>
            <%--            <li><a href="/admin/RD/management/revision">Revision</a></li>--%>
            <li><a href="/admin/RF/management/approved">Approved ${formName}</a></li>
            <li><a href="/admin/RF/management/effective">Effective ${formName}</a></li>
            <li><a href="/admin/RF/management/superseded">Superseded ${formName}</a></li>
<%--            <li><a href="/admin/RF/management/retirement">Retirement RF</a></li>--%>
        </ul>

    </li>
    <li>
        <a href="#" aria-expanded="false">
            <i class="pli-monitor-3"></i>
            <span class="menu-title">Training Management</span>
            <i class="arrow"></i>
        </a>

        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <li>
                <a href="#" aria-expanded="false">
                    <span class="menu-title">SOP Training Management</span>
                    <i class="arrow"></i>
                </a>
                <!--Submenu-->
                <ul class="collapse" aria-expanded="false">
                    <li><a href="/admin/training/sop/trainingLog">SOP Training Log</a></li>
                    <li><a href="/admin/training/sop/offline-training">Off-line Training</a></li>
                    <li><a href="/admin/training/sop/refresh-training">Refresh Training</a></li>
                    <li><a href="/admin/training/sop/matrix">SOP Training Matrix(Upload)</a></li>
                </ul>
            </li>
            <li>
                <a href="#" aria-expanded="false">
                    <span class="menu-title">ISO Training Management</span>
                    <i class="arrow"></i>
                </a>
                <!--Submenu-->
                <ul class="collapse" aria-expanded="false">
                    <li><a href="/admin/training/iso/trainingLog">ISO Training Log</a></li>
                    <li><a href="/admin/training/iso/offline-training">Off-line Training</a></li>
                    <li><a href="/admin/training/iso/training-certification">ISO Certificate</a></li>
<%--                    <li><a href="/admin/training/iso/refresh-training">Refresh Training</a></li>--%>
<%--                    <li><a href="/admin/training/iso/matrix">ISO Training Matrix(Upload)</a></li>--%>
                </ul>
            </li>
        </ul>
    </li>
    <li>
        <a href="/admin/authority" aria-expanded="false">
            <i class="pli-checked-user"></i>
            <span class="menu-title">User Management</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <li><a href="/admin/authority/users">???????????????</a></li>
            <li><a href="/admin/authority/accounts">???????????????</a></li>
            <li><a href="/admin/department">?????? ??????</a></li>
            <li><a href="/admin/role">ROLE ??????</a></li>
            <li><a href="/admin/authority/agreement-to-collect-and-use">???????????? ????????????</a></li>
            <li><a href="/admin/authority/confidentiality-pledge">?????? ?????? ????????????</a></li>
            <li><a href="/admin/authority/non-disclosure-agreement-for-sop">SOP ????????? ??????</a></li>
            <li><a href="/admin/authority/agreements-withdrawal">?????? ?????? ??????</a></li>
        </ul>
    </li>
    <li>
        <a href="#" aria-expanded="false">
            <i class="pli-check"></i>
            <span class="menu-title">????????????</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <li><a href="/admin/approval">???????????????</a></li>
            <li><a href="/admin/approval/temp">???????????????</a></li>
            <li><a href="/admin/approval/request">?????????</a></li>
<%--            <li><a href="/admin/approval/progress">?????????</a></li>--%>
            <li><a href="/admin/approval/approved">?????????</a></li>
            <li><a href="/admin/approval/rejected">?????????</a></li>
        </ul>
    </li>
    <li>
        <a href="#" aria-expanded="false">
            <i class="pli-security-camera"></i>
            <span class="menu-title">Security</span>
            <i class="arrow"></i>
        </a>
        <!--Submenu-->
        <ul class="collapse" aria-expanded="false">
            <li><a href="/admin/iso/accessLog">ISO Access Log</a></li>
            <li><a href="/admin/document/accessLog">SOP/${formName} Access Log</a></li>
            <li><a href="/admin/training/accessLog">Training Access Log</a></li>
<%--            <li><a href="#" style="color:red;">Change Control(?????????)</a></li>--%>
            <li><a href="/admin/change-control">Change Control</a></li>

        </ul>

    </li>
</ul>
