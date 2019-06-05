package com.itheima.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import com.itheima.domain.User;
import com.itheima.service.UserService;
import com.itheima.utils.CommonsUtils;
import com.itheima.utils.MailUtils;

public class UserServlet extends BaseServlet {
	
	//注销登录
		public void loginout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
		}
		
	
	//用户登录
	public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		UserService service = new UserService();
		User user = null;
		
		user = service.login(username,password);
		if(user != null){
			String autoLogin = request.getParameter("autoLogin");
			if("autoLogin".equals(autoLogin)){
				//自动登陆
				Cookie cookie_username = new Cookie("cookie_username",user.getUsername());
				cookie_username.setMaxAge(10*60);
				
				Cookie cookie_password = new Cookie("cookie_password",user.getPassword());
				cookie_password.setMaxAge(10*60);
				response.addCookie(cookie_username);
				response.addCookie(cookie_password);
			}
			session.setAttribute("user", user);
			
			response.sendRedirect(request.getContextPath()+"/index.jsp");
			
		}else{
			request.setAttribute("loginError", "��¼ʧ��");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
		}
	}
	
    
	//用户激活
	public void active(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	//��ü�����
			String activeCode  = request.getParameter("activeCode");
			UserService service =  new UserService();
			int isSuccess = service.active(activeCode);
			if(isSuccess > 0){
				response.sendRedirect(request.getContextPath()+"/login.jsp");
			}else{
				response.sendRedirect(request.getContextPath()+"/registerFail.jsp");
			}
	}
	
	//�û�ע��
	public void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = new User();
		Map<String,String[]> properties = request.getParameterMap();
		try {
			ConvertUtils.register(new Converter(){
				@Override
				public Object convert(Class clazz,Object value){
					//将String改为Date
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date parse = null;
					try {
						parse = format.parse(value.toString());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return parse;
				}
					},Date.class);
			//映射封装
			BeanUtils.populate(user, properties);
		} catch (IllegalAccessException e) {
			
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			
			e.printStackTrace();
		}
	
		user.setUid(CommonsUtils.getUUID());
		user.setTelephone(null);
		String activeCode = CommonsUtils.getUUID();
		user.setCode(activeCode);
		user.setState(0);
		
		UserService service = new UserService();
		boolean isRegisterSuccess = service.register(user);
		if(isRegisterSuccess){
			//���ͼ����ʼ�
			String  emailMsg = "点击此链接确认激活<a href='http://localhost:8080/HeimaShop1/user?method=active&activeCode="+activeCode+"'>"
					+ "http://localhost:8080/HeimaSHop1/user?method=active&activeCode="+activeCode+"</a>";
			try {
				MailUtils.sendMail(user.getEmail(),emailMsg);
			}  catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response.sendRedirect(request.getContextPath()+"/registerSuccess.jsp");
		}else{
			response.sendRedirect(request.getContextPath()+"/registerFail.jsp");
		}
	}
}
