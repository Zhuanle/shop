package com.itheima.dao;

import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import com.itheima.domain.User;
import com.itheima.utils.DataSourceUtils;

public class UserDao {

	public int register(User user) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "insert into user values (?,?,?,?,?,?,?,?,?,?)";
		return runner.update(sql,user.getUid(),user.getUsername(),user.getPassword(),user.getName(),user.getEmail(),user.getTelephone(),user.getBirthday(),user.getSex(),user.getState(),user.getCode() );
		
	}

	

	public int active(String activeCode) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql="update user set state=? where code =?";
		return runner.update(sql, 1,activeCode);
		
	}

	public User login(String username, String password) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select* from user where username=? and password=?";
		return (User) runner.query(sql, new BeanHandler<User>(User.class), username,password);
	}

}
