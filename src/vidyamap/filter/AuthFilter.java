package vidyamap.filter;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import vidyamap.util.Util;

public class AuthFilter implements Filter {

	static void log(Object o) {
		System.out.println(o.toString());
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		HttpSession session = req.getSession(false);
		
		if (!isAuth(session)) {
			resp.sendRedirect("/VidyaMap/index.html");
			return; 
		} else {
			if(req.getRequestURI().endsWith("signup.html")){
				Object privLevelObj = session.getAttribute("privLevel");
				if(privLevelObj != null){
					int privLevel = Integer.parseInt(privLevelObj.toString());
					if(privLevel < 5){
						resp.sendRedirect("/VidyaMap/html/main.html");
						return;
					}
				} else {
					resp.sendRedirect("/VidyaMap/index.html");
					return;
				}
			} 
			if(req.getRequestURI().endsWith("group_present.html")){
				String userType = session.getAttribute("userType").toString();
				Properties tableProps = Util.getTableProperties();
				if(!userType.equalsIgnoreCase(tableProps.getProperty("GROUP_TYPE"))){
					resp.sendRedirect("/VidyaMap/html/main.html");
					return;
				} 
			}
			if(req.getRequestURI().endsWith("main.html")){
				Object subject = session.getAttribute("subject");
				if(subject == null){
					resp.sendRedirect("/VidyaMap/html/session.html");
					return;
				}
				String userType = session.getAttribute("userType").toString();
				Properties tableProps = Util.getTableProperties();
				if(userType.equalsIgnoreCase(tableProps.getProperty("GROUP_TYPE"))){
					Object obj = session.getAttribute("presentStudents");
					if(obj == null){
						resp.sendRedirect("/VidyaMap/html/group_present.html");
						return;
					}
				} 
			}
		}
		
		chain.doFilter(request, response);
	}

	private boolean isAuth(HttpSession session) {
		if (session != null) {
			log("Session not null");
			return true;
		} else {
			log("Session is null");
			return false;
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
