package com.itheima.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import com.google.gson.Gson;
import com.itheima.domain.Cart;
import com.itheima.domain.CartItem;
import com.itheima.domain.Category;
import com.itheima.domain.Order;
import com.itheima.domain.OrderItem;
import com.itheima.domain.PageBean;
import com.itheima.domain.Product;
import com.itheima.domain.User;
import com.itheima.service.ProductService;
import com.itheima.utils.CommonsUtils;
import com.itheima.utils.JedisPoolUtils;
import com.itheima.utils.PaymentUtil;

import redis.clients.jedis.Jedis;

public class ProductServlet extends BaseServlet {
	
	//获得我的订单
	public void  myOrders(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		ProductService service = new ProductService();
		List<Order> orderList = service.findAllOrders(user);
		//循环所有订单  为每个订单填充集合项信息
		if(orderList != null){
			for(Order order:orderList){
				String oid = order.getOid();
				//查询该订单的所有订单项
				List<Map<String, Object>> orderItemList = service.findAllOderItemByOid(oid); 
		       //将mapList转换为orderItem
				for(Map<String,Object> map:orderItemList){
					//从orderItemList中取出封装到Order Item中
					OrderItem item= new OrderItem();
					try {
						BeanUtils.populate(item, map);
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//从orderItemList中取出封装到product
					Product product = new Product();
					try {
						BeanUtils.populate(product, map);
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//将product封装到oderItem
					item.setProduct(product);
					
					//将orderItem封装到orderItemList
				order.getOrderItems().add(item);
				}
				
			}
		}
		//orderList封装完毕
		request.setAttribute("orderList", orderList);
		request.getRequestDispatcher("/order_list.jsp").forward(request, response);
		}
	
	
	
	
	//确认订单  -- 更新收货人信息  在线支付
	public void confirmOrder(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		//1、更新收货人信息
		Map<String,String[]> properties = request.getParameterMap();
		Order order = new Order();
		
		try {
			BeanUtils.populate(order, properties);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ProductService service = new ProductService();
		service.updateOrderAddr(order);
		//2、在线支付
	//	String pd_frpId = request.getParameter("pd_frpId");
		
		//只接入一个接口，这个接口集成所有银行接口
		//易宝
		// 获得 支付必须基本数据
				String orderid = request.getParameter("oid");
				String money = order.getTotal()+"";
				// 银行
				String pd_FrpId = request.getParameter("pd_FrpId");

				// 发给支付公司需要哪些数据
				String p0_Cmd = "Buy";
				String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
				String p2_Order = orderid;
				String p3_Amt = money;
				String p4_Cur = "CNY";
				String p5_Pid = "";
				String p6_Pcat = "";
				String p7_Pdesc = "";
				// 支付成功回调地址 ---- 第三方支付公司会访问、用户访问
				// 第三方支付可以访问网址
				String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
				String p9_SAF = "";
				String pa_MP = "";
				String pr_NeedResponse = "1";
				// 加密hmac 需要密钥
				String keyValue = ResourceBundle.getBundle("merchantInfo").getString(
						"keyValue");
				String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt,
						p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, p9_SAF, pa_MP,
						pd_FrpId, pr_NeedResponse, keyValue);
				
				
				String url = "https://www.yeepay.com/app-merchant-proxy/node?pd_FrpId="+pd_FrpId+
								"&p0_Cmd="+p0_Cmd+
								"&p1_MerId="+p1_MerId+
								"&p2_Order="+p2_Order+
								"&p3_Amt="+p3_Amt+
								"&p4_Cur="+p4_Cur+
								"&p5_Pid="+p5_Pid+
								"&p6_Pcat="+p6_Pcat+
								"&p7_Pdesc="+p7_Pdesc+
								"&p8_Url="+p8_Url+
								"&p9_SAF="+p9_SAF+
								"&pa_MP="+pa_MP+
								"&pr_NeedResponse="+pr_NeedResponse+
								"&hmac="+hmac;

				//重定向到第三方支付平台
				response.sendRedirect(url);
			
	}
	
	
	//提交订单
	public void submitOrder(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		
		HttpSession session = request.getSession();
		
		//判断用户是否已经登录
		User user = (User) session.getAttribute("user");
		//封装一个Order对象
		Order order = new Order();
		
		//该订单的订单号
		String oid = CommonsUtils.getUUID();
		order.setOid(oid);
		
		//下单时间
		order.setOrdertime(new Date());
		
		//获得订单总金额
		Cart cart = (Cart) session.getAttribute("cart");
		order.setTotal(cart.getTotal());
		
		//订单状态
		order.setState(0);
		
		//收货地址
		order.setAddress(null);
		
		//收货人
		order.setName(null);
		
		//收货人电话
		order.setTelephone(null);
		
		//订单属于哪一个用户
		order.setUser(null);
		
		//该订单中有多少订单项
			//获得购物车中的购物项
		   Map<String,CartItem> cartItems = cart.getCartItems();
		   for(Map.Entry<String, CartItem>entry:cartItems.entrySet()){
			   CartItem cartItem = entry.getValue();
			   OrderItem orderItem = new OrderItem();
			   //该订单的订单项
			   orderItem.setItemid(CommonsUtils.getUUID());
			   
			   //订单项中的购买数量
			   orderItem.setCount(cartItem.getBuyNum());
			   
			   //订单项小计
			   orderItem.setSubtotal(cartItem.getSubtotal());
			   
			  //订单项内部的商品
			   orderItem.setProduct(cartItem.getProduct());
			   
			   //该订单项属于哪一个订单
			   orderItem.setOrder(order);
	
			   //将该订单项加入到订单项的集合中
			   order.getOrderItems().add(orderItem);
		   }
	
		   //传递数据到service层
		   ProductService service = new ProductService();
		   service.submitOrder(order);
		    
		   session.setAttribute("order", order);
		   //页面跳转
		   response.sendRedirect(request.getContextPath()+"/order_info.jsp");
	}
	
	
	//删除单一商品
	public void delProFromCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//获得要删除的item的pid
		String pid = request.getParameter("pid");
		//删除session中的购物测中的购物项集合中的item
		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");
		if(cart != null){
			Map<String,CartItem> cartItems = cart.getCartItems();
			//需要修改总价
			cart.setTotal(cart.getTotal()-cartItems.get(pid).getSubtotal());
			//删除
			cartItems.remove(pid);
			cart.setCartItems(cartItems);
		}
		
		session.setAttribute("cart", cart);
		
		request.getRequestDispatcher("cart.jsp").forward(request, response);
	}
	
	//清空购物车
	public void clearCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.removeAttribute("cart");
	    
		request.getRequestDispatcher("cart.jsp").forward(request, response);
	}
	
	//将商品添加到购物车
	
	public void addProductToCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
      
		HttpSession session = request.getSession();
		
		String pid = request.getParameter("pid");
		int buyNum = Integer.parseInt(request.getParameter("buyNum"));
		
		
		ProductService service = new ProductService();
		Product product = service.findProductByPid(pid);
		
		double subtotal  =  product.getShop_price()*buyNum;
		
		//封装CartItem
		CartItem item = new CartItem();
		item.setProduct(product);
		item.setBuyNum(buyNum);
		item.setSubtotal(subtotal);
		
		//获得购物车，判断session中是否已经存在购物车
		
		Cart cart = (Cart) session.getAttribute("cart");
		
		if(cart == null){
			cart = new Cart();
		}
		
		//判断购物车中是否包含此商品  --key是pid
		//如果购物车中有此商品--现在的数量与原有数量相加
		Map<String,CartItem> cartItems = cart.getCartItems();
		double newSubtotal = 0; 
		
		if(cartItems.containsKey(pid)){
			item = cartItems.get(pid);
			//取出原有商品数量	
			int oldNum = item.getBuyNum();
			oldNum += buyNum;
			
			item.setBuyNum(oldNum);
		//cart.setCartItems(cartItems);
		
			//修改小计
			
			//原来该商品的小计
			double oldSubtotal = item.getSubtotal();
			//新买的商品的小计
			newSubtotal = buyNum*product.getShop_price();
			item.setSubtotal(oldSubtotal+newSubtotal);
		
		}
		else{
			cart.getCartItems().put(product.getPid(), item);
			newSubtotal = buyNum*product.getShop_price();
		//	cart.setTotal(newSubtotal);
		}
		//计算总计
		double total = 0;
		total = cart.getTotal()+newSubtotal;
		
		cart.getCartItems().put(product.getPid(), item);
		cart.setTotal(total);
		//将购物车再次放回session
		session.setAttribute("cart", cart);
		
		//直接跳转到购物车
		request.getRequestDispatcher("cart.jsp").forward(request, response);
	}
	
	
	
	// 获得商品类型
	public void categoryList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ProductService service = new ProductService();

		// 1、获得jedis对象连接redis数据库

		Jedis jedis = JedisPoolUtils.getJedis();
		String categoryListJson = jedis.get("categoryListJson");
		// 2、判断categoryListJson是否为空
		if (categoryListJson == null) {
			System.out.println("缓存没有，从数据库获取");
			// 从数据库取数据
			List<Category> categoryList = service.findAllCategory();

			Gson gson = new Gson();
			categoryListJson = gson.toJson(categoryList);

			jedis.set("categoryListJson", categoryListJson);

		}

		response.setContentType("text/html;charset=UTF-8");

		response.getWriter().write(categoryListJson);
	}
	

	// 首页
	public void index(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		ProductService service = new ProductService();

		// 热门商品
		List<Product> hotProduct = service.foundHotProduct();

		// 最新商品

		List<Product> newProduct = service.foundNewProduct();

		request.setAttribute("hotProductList", hotProduct);
		request.setAttribute("newProductList", newProduct);

		// 商品类型

		List<Category> allType = service.findAllCategory();
		request.setAttribute("allTypeCategory", allType);
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}
	

	// 显示商品的详细信息
	public void productInfo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pid = request.getParameter("pid");
		String currentPage = request.getParameter("currentPage");
		String cid = request.getParameter("cid");

		ProductService service = new ProductService();
		Product product = service.findProductByPid(pid);

		request.setAttribute("product", product);
		request.setAttribute("cid", cid);
		request.setAttribute("currentPage", currentPage);

		String pids = pid;

		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("pids".equals(cookie.getName())) {
					pids = cookie.getValue();
					String[] split = pids.split("-");
					List<String> aslist = Arrays.asList(split);
					LinkedList<String> list = new LinkedList<String>(aslist);
					// 判断集合中是否包含当前商品的pid
					if (list.contains(pid)) {
						list.remove(pid);
					}
					list.addFirst(pid);

					// 将集合转化为字符串
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < list.size() && i < 7; i++) {
						sb.append(list.get(i));
						if (i != list.size() - 1) {
							sb.append("-");
						}
					}
					pids = sb.toString();

				}
			}
		}

		// 向客户端添加Cookie
		Cookie cookie_pids = new Cookie("pids", pids);
		response.addCookie(cookie_pids);

		request.getRequestDispatcher("/product_info.jsp").forward(request, response);
	}

	
	
	// 通过cid获得商品列表
	public void productList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String cid = request.getParameter("cid");
		String currentPagestr = request.getParameter("currentPage");
		if (currentPagestr == null) {
			currentPagestr = "1";
		}
		int currentPage = Integer.parseInt(currentPagestr);
		int currentCount = 12;
		ProductService service = new ProductService();
		PageBean pageBean = service.findProductListByCid(cid, currentPage, currentCount);

		request.setAttribute("pageBean", pageBean);
		request.setAttribute("cid", cid);

		List<Product> historyProductList = new ArrayList<Product>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("pids".equals(cookie.getName())) {
					String pids = cookie.getValue();
					String[] split = pids.split("-");
					for (String pid : split) {
						Product pro = service.findProductByPid(pid);
						historyProductList.add(pro);
					}
				}
			}
		}

		request.setAttribute("historyProductList", historyProductList);
		request.getRequestDispatcher("/product_list.jsp").forward(request, response);

	}
}
