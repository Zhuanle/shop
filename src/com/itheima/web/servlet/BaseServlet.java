package com.itheima.web.servlet;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("all")
public class BaseServlet extends HttpServlet {
	@Override
	protected void service(HttpServletRequest req,HttpServletResponse resp)throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
		
		
		try {
			//1、获得要执行的方法名称
			String methodName = req.getParameter("method");
			//2、获得当前被访问的对象的字节码对象
			Class clazz = this.getClass();
			//3、获得字节码对象的中的指定方法
			Method method = clazz.getMethod(methodName, HttpServletRequest.class,HttpServletResponse.class);
			//4、执行相应功能方法
			method.invoke(this, req,resp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
}
