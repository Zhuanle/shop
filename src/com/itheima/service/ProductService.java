package com.itheima.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.itheima.dao.ProductDao;
import com.itheima.domain.Category;
import com.itheima.domain.Order;
import com.itheima.domain.OrderItem;
import com.itheima.domain.PageBean;
import com.itheima.domain.Product;
import com.itheima.domain.User;
import com.itheima.utils.DataSourceUtils;



public class ProductService {

	ProductDao  dao  = new ProductDao();
	public List<Product> foundHotProduct() {
		// TODO Auto-generated method stub
		
		List<Product> hotProduct = null;
		try {
			hotProduct = dao.foundHotProduct();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hotProduct;
	}

	public List<Product> foundNewProduct(){
		// TODO Auto-generated method stub
       List<Product> newProduct = null; 
       try {
		newProduct = dao.foundNewProduct();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		return newProduct;
	}

	public List<Category> findAllCategory() {
		// TODO Auto-generated method stub
		List<Category> allType = null;
		try {
			allType = dao.findAllCategory();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allType;
	}

	//商品分页
	public PageBean findProductListByCid(String cid,int currentPage,int currentCount) {
		// TODO Auto-generated method stub
	     ProductDao dao = new ProductDao();
	
		//封装一个PageBean 返回web层
		PageBean<Product> pageBean = new PageBean<Product>();
		
	   //1、封装当前页
		pageBean.setCurrentPage(currentPage);
		//2、每页显示的条数
		pageBean.setCurrentCount(currentCount);
		//3、封装总条数
		int totalCount = 0;
		try {
			totalCount = dao.getCount(cid);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pageBean.setTotalCount(totalCount);
		//4、封装总页数
		int totalPage = (int) Math.ceil(1.0*totalCount/currentCount);
		pageBean.setTotalPage(totalPage);
		//5、封装当前页显示的数据
		List<Product> list = null;
		int index = (currentPage-1)*currentCount;
		try {
			list = dao.findProductListByCid(cid,index,currentCount);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pageBean.setList(list);
		return pageBean;
		
	}

	
    //查询商品详情
	public Product findProductByPid(String pid) {
		ProductDao dao = new ProductDao();
		Product product = null;
		try {
			product = dao.findProductByPid(pid);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return product;
	}

	//提交订单   将订单和订单项存储到数据库中
	public void submitOrder(Order order) {
		
		ProductDao dao = new ProductDao();
		try {
			//1、开启事务
			DataSourceUtils.startTransaction();
			
			//2、调用dao存储order表数据的方法
			dao.addOrder(order);
			//3、、调用dao存储orderItem表数据的方法
			dao.addOrderItem(order.getOrderItems());
		} catch (SQLException e) {
			try {
				DataSourceUtils.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally{
			try {
				DataSourceUtils.commitAndRelease();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public void updateOrderAddr(Order order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderAddr(order);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void upOrderState(String r6_Order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderState(r6_Order);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public List<Order> findAllOrders(User user) {
		ProductDao dao = new ProductDao();
		List<Order> orders=null;
		try {
			orders = dao.finAllOrders(user.getUid());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return orders;
	}

	public List<Map<String, Object>> findAllOderItemByOid(String oid) {
		ProductDao dao = new ProductDao();
		List<Map<String, Object>> orderItemList = null;
		try {
			orderItemList = dao.findAllOrderItemByOid(oid);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return orderItemList;
	}
}
