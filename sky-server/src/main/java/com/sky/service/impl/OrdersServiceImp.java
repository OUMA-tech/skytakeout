package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.apache.commons.lang.RandomStringUtils;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Signature;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImp implements OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // deal with errors
        // check address
        AddressBook addressBook = addressMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // check shopping cart
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = cartMapper.list(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // insert data to table orders
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setAddress(addressBook.getDetail());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        ordersMapper.insert(orders);

        // insert multiple data to table order_detail
        List<OrderDetail> orderDetailsList = new ArrayList<>();
        shoppingCartList.forEach(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailsList.add(orderDetail);
        });
        orderDetailMapper.insertBatch(orderDetailsList);
        // clean shopping cart
        cartMapper.clean(userId);
        // pack up return value
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();


        return orderSubmitVO;
    }

    @Override
    public PageResult historyOrders(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setPage(page);
        ordersPageQueryDTO.setPageSize(pageSize);

        Page<Orders> pageResult = ordersMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> records = new ArrayList<>();
        if(pageResult!=null && !pageResult.isEmpty()){
            pageResult.getResult().forEach(orders -> {
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                records.add(orderVO);
            });
        }

        return new PageResult(pageResult.getTotal(), records);
    }

    @Override
    public OrderVO orderDetail(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        Orders orders = ordersMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void cancel(Long id) {
//        orderDetailMapper.deleteBatch(id);
        Orders orders = ordersMapper.getById(id);
        if (orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //order status 1 waiting payment 2 waiting accept by shop 3 accepted 4 delivering 5 finished 6 canceled
        if (orders.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // if the order is paid, need to refund it,
        // but I don't have the payment system, so I just cancel it
//        if (orders.getPayStatus() == Orders.PAID){
//            // refund
//            refund logic
//        }

        Orders ordersUpdate = new Orders();
        ordersUpdate.setId(id);
        ordersUpdate.setStatus(Orders.CANCELLED);
        ordersUpdate.setCancelReason("user cancel");
        ordersUpdate.setCancelTime(LocalDateTime.now());
        ordersMapper.update(ordersUpdate);
    }

    @Override
    public void repetition(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        if (orderDetailList == null || orderDetailList.isEmpty()){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
            ShoppingCart cart = new ShoppingCart();
            cart.setCreateTime(LocalDateTime.now());
            cart.setUserId(userId);
            BeanUtils.copyProperties(orderDetail, cart);
            return cart;
        }).collect(Collectors.toList());

        cartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO)  {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));

//       Didn't implement payment system api with WeChatPayUtil, so I use fake VO
        OrderPaymentVO vo = OrderPaymentVO.builder()
                .nonceStr(String.valueOf(System.currentTimeMillis() / 1000))
                .nonceStr(RandomStringUtils.randomNumeric(32))
                .signType("RSA")
                .paySign("dGVzdA==")
                .packageStr("test")
                .build();
        return vo;
    }


    @Override
    public PageResult adminConditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.pageQuery(ordersPageQueryDTO);
        if (page != null && page.getResult() != null){
            List<OrderVO> orderVOList = page.getResult().stream().map(orders -> {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishes(orders);
                orderVO.setOrderDishes(orderDishes);
                return orderVO;
            }).collect(Collectors.toList());
            return new PageResult(page.getTotal(), orderVOList);
        }
        return null;
    }

    @Override
    public OrderStatisticsVO adminStatistics() {
        Integer confirmedOrder = ordersMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgressOrder = ordersMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmedOrder = ordersMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmedOrder);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgressOrder);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmedOrder);
        return orderStatisticsVO;
    }

    @Override
    public void adminConfirmOrder(OrdersDTO ordersDTO) {
        Long orderId = ordersDTO.getId();
        Orders orders = ordersMapper.getById(orderId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        } else if (orders.getStatus() != 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders ordersUpdate = new Orders();
        ordersUpdate.setId(orderId);
        ordersUpdate.setStatus(Orders.CONFIRMED);
        ordersMapper.update(ordersUpdate);
    }

    @Override
    public void adminRejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Long orderId = ordersRejectionDTO.getId();
        Orders orders = ordersMapper.getById(orderId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        } else if (orders.getStatus() != 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // deal with refund logic
//        Integer payStatus = orders.getPayStatus();
//        if (payStatus == Orders.PAID) {
//
//        }

        // update orders database
        Orders ordersUpdate = Orders.builder()
                .id(orderId)
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        ordersMapper.update(ordersUpdate);
    }

    @Override
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = ordersMapper.getById(ordersCancelDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        } else if (orders.getStatus() > 3) {
            // order status 1 waiting payment 2 waiting accept by shop 3 accepted 4 delivering 5 finished 6 canceled
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders ordersUpdate = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        ordersMapper.update(ordersUpdate);
    }

    @Override
    public void adminDeliveryOrder(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        } else if (!orders.getStatus().equals(Orders.CONFIRMED)) {
            // order status 1 waiting payment 2 waiting accept by shop 3 accepted 4 delivering 5 finished 6 canceled
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders ordersUpdate = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        ordersMapper.update(ordersUpdate);
    }

    @Override
    public void adminCompleteOrder(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        } else if (!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            // order status 1 waiting payment 2 waiting accept by shop 3 accepted 4 delivering 5 finished 6 canceled
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders ordersUpdate = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        ordersMapper.update(ordersUpdate);
    }

    private String getOrderDishes(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        if (orderDetailList != null && !orderDetailList.isEmpty()){
            List<String> orderDishes = orderDetailList.stream().map(orderDetail -> {
                return orderDetail.getName() + "*" + orderDetail.getNumber();
            }).collect(Collectors.toList());
            return String.join(",", orderDishes);
        }
        return "";
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);

        // using websocket to inform shop
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "Order " + ordersDB.getId() + " has been paid successfully");

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public void reminder(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2); // 1: order submitted, 2: order reminder
        map.put("orderId", orders.getId());
        map.put("content", "Order " + orders.getId() + " reminder");

        // using websocket send notify reminder to shop
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

}
