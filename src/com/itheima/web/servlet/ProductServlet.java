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
	
	//����ҵĶ���
	public void  myOrders(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		ProductService service = new ProductService();
		List<Order> orderList = service.findAllOrders(user);
		//ѭ�����ж���  Ϊÿ��������伯������Ϣ
		if(orderList != null){
			for(Order order:orderList){
				String oid = order.getOid();
				//��ѯ�ö��������ж�����
				List<Map<String, Object>> orderItemList = service.findAllOderItemByOid(oid); 
		       //��mapListת��ΪorderItem
				for(Map<String,Object> map:orderItemList){
					//��orderItemList��ȡ����װ��Order Item��
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
					//��orderItemList��ȡ����װ��product
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
					
					//��product��װ��oderItem
					item.setProduct(product);
					
					//��orderItem��װ��orderItemList
				order.getOrderItems().add(item);
				}
				
			}
		}
		//orderList��װ���
		request.setAttribute("orderList", orderList);
		request.getRequestDispatcher("/order_list.jsp").forward(request, response);
		}
	
	
	
	
	//ȷ�϶���  -- �����ջ�����Ϣ  ����֧��
	public void confirmOrder(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		//1�������ջ�����Ϣ
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
		//2������֧��
	//	String pd_frpId = request.getParameter("pd_frpId");
		
		//ֻ����һ���ӿڣ�����ӿڼ����������нӿ�
		//�ױ�
		// ��� ֧�������������
				String orderid = request.getParameter("oid");
				String money = order.getTotal()+"";
				// ����
				String pd_FrpId = request.getParameter("pd_FrpId");

				// ����֧����˾��Ҫ��Щ����
				String p0_Cmd = "Buy";
				String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
				String p2_Order = orderid;
				String p3_Amt = money;
				String p4_Cur = "CNY";
				String p5_Pid = "";
				String p6_Pcat = "";
				String p7_Pdesc = "";
				// ֧���ɹ��ص���ַ ---- ������֧����˾����ʡ��û�����
				// ������֧�����Է�����ַ
				String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
				String p9_SAF = "";
				String pa_MP = "";
				String pr_NeedResponse = "1";
				// ����hmac ��Ҫ��Կ
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

				//�ض��򵽵�����֧��ƽ̨
				response.sendRedirect(url);
			
	}
	
	
	//�ύ����
	public void submitOrder(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		
		HttpSession session = request.getSession();
		
		//�ж��û��Ƿ��Ѿ���¼
		User user = (User) session.getAttribute("user");
		//��װһ��Order����
		Order order = new Order();
		
		//�ö����Ķ�����
		String oid = CommonsUtils.getUUID();
		order.setOid(oid);
		
		//�µ�ʱ��
		order.setOrdertime(new Date());
		
		//��ö����ܽ��
		Cart cart = (Cart) session.getAttribute("cart");
		order.setTotal(cart.getTotal());
		
		//����״̬
		order.setState(0);
		
		//�ջ���ַ
		order.setAddress(null);
		
		//�ջ���
		order.setName(null);
		
		//�ջ��˵绰
		order.setTelephone(null);
		
		//����������һ���û�
		order.setUser(null);
		
		//�ö������ж��ٶ�����
			//��ù��ﳵ�еĹ�����
		   Map<String,CartItem> cartItems = cart.getCartItems();
		   for(Map.Entry<String, CartItem>entry:cartItems.entrySet()){
			   CartItem cartItem = entry.getValue();
			   OrderItem orderItem = new OrderItem();
			   //�ö����Ķ�����
			   orderItem.setItemid(CommonsUtils.getUUID());
			   
			   //�������еĹ�������
			   orderItem.setCount(cartItem.getBuyNum());
			   
			   //������С��
			   orderItem.setSubtotal(cartItem.getSubtotal());
			   
			  //�������ڲ�����Ʒ
			   orderItem.setProduct(cartItem.getProduct());
			   
			   //�ö�����������һ������
			   orderItem.setOrder(order);
	
			   //���ö�������뵽������ļ�����
			   order.getOrderItems().add(orderItem);
		   }
	
		   //�������ݵ�service��
		   ProductService service = new ProductService();
		   service.submitOrder(order);
		    
		   session.setAttribute("order", order);
		   //ҳ����ת
		   response.sendRedirect(request.getContextPath()+"/order_info.jsp");
	}
	
	
	//ɾ����һ��Ʒ
	public void delProFromCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//���Ҫɾ����item��pid
		String pid = request.getParameter("pid");
		//ɾ��session�еĹ�����еĹ�������е�item
		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");
		if(cart != null){
			Map<String,CartItem> cartItems = cart.getCartItems();
			//��Ҫ�޸��ܼ�
			cart.setTotal(cart.getTotal()-cartItems.get(pid).getSubtotal());
			//ɾ��
			cartItems.remove(pid);
			cart.setCartItems(cartItems);
		}
		
		session.setAttribute("cart", cart);
		
		request.getRequestDispatcher("cart.jsp").forward(request, response);
	}
	
	//��չ��ﳵ
	public void clearCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.removeAttribute("cart");
	    
		request.getRequestDispatcher("cart.jsp").forward(request, response);
	}
	
	//����Ʒ��ӵ����ﳵ
	
	public void addProductToCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
      
		HttpSession session = request.getSession();
		
		String pid = request.getParameter("pid");
		int buyNum = Integer.parseInt(request.getParameter("buyNum"));
		
		
		ProductService service = new ProductService();
		Product product = service.findProductByPid(pid);
		
		double subtotal  =  product.getShop_price()*buyNum;
		
		//��װCartItem
		CartItem item = new CartItem();
		item.setProduct(product);
		item.setBuyNum(buyNum);
		item.setSubtotal(subtotal);
		
		//��ù��ﳵ���ж�session���Ƿ��Ѿ����ڹ��ﳵ
		
		Cart cart = (Cart) session.getAttribute("cart");
		
		if(cart == null){
			cart = new Cart();
		}
		
		//�жϹ��ﳵ���Ƿ��������Ʒ  --key��pid
		//������ﳵ���д���Ʒ--���ڵ�������ԭ���������
		Map<String,CartItem> cartItems = cart.getCartItems();
		double newSubtotal = 0; 
		
		if(cartItems.containsKey(pid)){
			item = cartItems.get(pid);
			//ȡ��ԭ����Ʒ����	
			int oldNum = item.getBuyNum();
			oldNum += buyNum;
			
			item.setBuyNum(oldNum);
		//cart.setCartItems(cartItems);
		
			//�޸�С��
			
			//ԭ������Ʒ��С��
			double oldSubtotal = item.getSubtotal();
			//�������Ʒ��С��
			newSubtotal = buyNum*product.getShop_price();
			item.setSubtotal(oldSubtotal+newSubtotal);
		
		}
		else{
			cart.getCartItems().put(product.getPid(), item);
			newSubtotal = buyNum*product.getShop_price();
		//	cart.setTotal(newSubtotal);
		}
		//�����ܼ�
		double total = 0;
		total = cart.getTotal()+newSubtotal;
		
		cart.getCartItems().put(product.getPid(), item);
		cart.setTotal(total);
		//�����ﳵ�ٴηŻ�session
		session.setAttribute("cart", cart);
		
		//ֱ����ת�����ﳵ
		request.getRequestDispatcher("cart.jsp").forward(request, response);
	}
	
	
	
	// �����Ʒ����
	public void categoryList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ProductService service = new ProductService();

		// 1�����jedis��������redis���ݿ�

		Jedis jedis = JedisPoolUtils.getJedis();
		String categoryListJson = jedis.get("categoryListJson");
		// 2���ж�categoryListJson�Ƿ�Ϊ��
		if (categoryListJson == null) {
			System.out.println("����û�У������ݿ��ȡ");
			// �����ݿ�ȡ����
			List<Category> categoryList = service.findAllCategory();

			Gson gson = new Gson();
			categoryListJson = gson.toJson(categoryList);

			jedis.set("categoryListJson", categoryListJson);

		}

		response.setContentType("text/html;charset=UTF-8");

		response.getWriter().write(categoryListJson);
	}
	

	// ��ҳ
	public void index(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		ProductService service = new ProductService();

		// ������Ʒ
		List<Product> hotProduct = service.foundHotProduct();

		// ������Ʒ

		List<Product> newProduct = service.foundNewProduct();

		request.setAttribute("hotProductList", hotProduct);
		request.setAttribute("newProductList", newProduct);

		// ��Ʒ����

		List<Category> allType = service.findAllCategory();
		request.setAttribute("allTypeCategory", allType);
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}
	

	// ��ʾ��Ʒ����ϸ��Ϣ
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
					// �жϼ������Ƿ������ǰ��Ʒ��pid
					if (list.contains(pid)) {
						list.remove(pid);
					}
					list.addFirst(pid);

					// ������ת��Ϊ�ַ���
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

		// ��ͻ������Cookie
		Cookie cookie_pids = new Cookie("pids", pids);
		response.addCookie(cookie_pids);

		request.getRequestDispatcher("/product_info.jsp").forward(request, response);
	}

	
	
	// ͨ��cid�����Ʒ�б�
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
