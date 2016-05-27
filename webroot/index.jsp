<%@ page import="java.net.InetAddress" %>
<%@ page import="java.net.NetworkInterface" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.net.InterfaceAddress" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=request.getAttribute("title") == null?"移动支付":request.getAttribute("title").toString()%></title>
</head>
<body>
	<table id="main" border="0" cellpadding="3" cellspacing="1" width="80%" align="center" style="background-color: #b9d8f3;">
		<tr style="text-align: center; COLOR: #0076C8; BACKGROUND-COLOR: #F4FAFF; font-weight: bold">
			<td colspan="2">
				<%
					Object message = request.getAttribute("message");

					if(message == null){
						out.println("intercommerce_v1服务信息");
					} else {
						out.println(message.toString());
					}
				%>
			</td>
		</tr>
		<%
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()){
				NetworkInterface ni = e.nextElement();

		%>
		<tr style="text-align: center; COLOR: #0076C8; BACKGROUND-COLOR: #F4FAFF; font-weight: bold">
			<td colspan="2">
				<%
					out.print("name=" + ni.getName() + "," + ni.toString());
				%>
			</td>
		</tr>
		<%
			for (InterfaceAddress ifa : ni.getInterfaceAddresses()){
				InetAddress iad = ifa.getAddress();
		%>
		<tr style="text-align: center; COLOR: #0076C8; BACKGROUND-COLOR: #F4FAFF; font-weight: bold">

			<td colspan="1">
				<%
					out.print(iad.getHostName());
				%>
			</td>
			<td colspan="1">
				<%
					out.print(iad.getHostAddress());
				%>
			</td>
		</tr>
		<%
				}
			}
		%>
		<tr style="text-align: center; COLOR: #0076C8; BACKGROUND-COLOR: #F4FAFF; font-weight: bold">
			<td colspan="2">
				虚拟机信息
			</td>
		</tr>
		<tr style="text-align: center; COLOR: #0076C8; BACKGROUND-COLOR: #F4FAFF; font-weight: bold">
			<td colspan="1">
				Java版本
			</td>
			<td colspan="1">
				<%
					out.print(System.getProperty("java.version"));
				%>
			</td>
		</tr>
	</table>
</body>
</html>