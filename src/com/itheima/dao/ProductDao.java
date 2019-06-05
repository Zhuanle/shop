package com.itheima.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.itheima.domain.Category;
import com.itheima.domain.Order;
import com.itheima.domain.OrderItem;
import com.itheima.domain.Product;
import com.itheima.utils.DataSourceUtils;

public class ProductDao {

	public List<Product> foundHotProduct() throws SQLException {
		// TODO Auto-generated method stub
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product where is_hot=? limit ?,?";
		return runner.query(sql, new BeanListHandler<Product>(Product.class), 1, 0, 9);

	}

	public List<Product> foundNewProduct() throws SQLException {
		// TODO Auto-generated method stub
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product order by pdate desc limit ?,?";
		return runner.query(sql, new BeanListHandler<Product>(Product.class), 0, 9);
	}

	public List<Category> findAllCategory() throws SQLException {
		// TODO Auto-generated method stub
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from Category";
		return runner.query(sql, new BeanListHandler<Category>(Category.class));
	}

	public int getCount(String cid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select count(*) from product where cid=?";
		Long query = (Long) runner.query(sql, new ScalarHandler(), cid);
		return query.intValue();
	}

	public List<Product> findProductListByCid(String cid, int index, int currentCount) throws SQLException {

		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select* from product where cid=? limit ?,?";
		return runner.query(sql, new BeanListHandler<Product>(Product.class), cid, index, currentCount);
	}

	public Product findProductByPid(String pid) throws SQLException {

		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select* from product where pid=?";
		return runner.query(sql, new BeanHandler<Product>(Product.class), pid);

	}

	public void addOrder(Order order) throws SQLException {
		QueryRunner runner = new QueryRunner();
		String sql = "insert into orders values(?,?,?,?,?,?,?,?)";
		Connection conn = DataSourceUtils.getConnection();

		runner.update(conn, sql, order.getOid(), order.getOrdertime(), order.getTotal(), order.getState(),
				order.getAddress(), order.getName(), order.getTelephone(), order.getUser().getUid());

	}

	public void addOrderItem(List<OrderItem> orderItems) throws SQLException {
		QueryRunner runner = new QueryRunner();
		String sql = "insert into orderitem values(?,?,?,?,?)";
		Connection conn = DataSourceUtils.getConnection();
		for (OrderItem orderItem : orderItems) {
			runner.update(conn, sql, orderItem.getItemid(), orderItem.getCount(), orderItem.getSubtotal(),
					orderItem.getProduct().getPid(), orderItem.getOrder().getOid());
		}

	}

	public void updateOrderAddr(Order order) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "update orders set address=?,name=?,telephone=? where oid=?";
		runner.update(sql,order.getAddress(),order.getName(),order.getTelephone(),order.getOid());
		
	}

	public void updateOrderState(String r6_Order) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "update orders set state = ? where oid=?";
		runner.update(sql,1, r6_Order);
		
	}

	public List<Order> finAllOrders(String uid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select* from orders where uid=?";
		return runner.query(sql, new BeanListHandler<Order>(Order.class), uid);
		
	}

	public List<Map<String, Object>> findAllOrderItemByOid(String oid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from orderitem i,product p where i.pid = p.pid&i.oid = ?";
		List<Map<String,Object>> mapList = runner.query(sql, new MapListHandler(), oid);
				return mapList;
	}

}
