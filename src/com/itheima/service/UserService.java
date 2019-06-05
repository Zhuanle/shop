package com.itheima.service;

import java.sql.SQLException;

import com.itheima.dao.UserDao;
import com.itheima.domain.User;

public class UserService {

	public boolean register(User user) {
		UserDao dao = new UserDao();
		int row = 0;
		try {
			row = dao.register(user);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return row>0?true:false;
	}

	
	//”√ªßº§ªÓ
	public int active(String activeCode) {
		UserDao dao = new UserDao();
		int isSuccess = 0;
		try {
			 isSuccess = dao.active(activeCode);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return  isSuccess;
	}

	public User login(String username, String password) {
		UserDao dao = new UserDao();
		User user = new User();
			 try {
				 user = dao.login(username,password);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return user;
		
		
		
	}

}
