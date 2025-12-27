package com.phegon.foodapp.order.services;

import com.phegon.foodapp.auth_users.entity.User;
import com.phegon.foodapp.auth_users.services.UserService;
import com.phegon.foodapp.cart.entity.Cart;
import com.phegon.foodapp.cart.entity.CartItem;
import com.phegon.foodapp.cart.repository.CartRepository;
    import com.phegon.foodapp.cart.services.CartService;
import com.phegon.foodapp.email_notification.dtos.NotificationDTO;
import com.phegon.foodapp.email_notification.services.NotificationService;
    import com.phegon.foodapp.enums.OrderStatus;
import com.phegon.foodapp.enums.PaymentStatus;
import com.phegon.foodapp.exceptions.BadRequestException;
import com.phegon.foodapp.exceptions.NotFoundException;
import com.phegon.foodapp.menu.dtos.MenuDTO;
import com.phegon.foodapp.order.dtos.OrderDTO;
    import com.phegon.foodapp.order.dtos.OrderItemDTO;
import com.phegon.foodapp.order.entity.Order;
import com.phegon.foodapp.order.entity.OrderItem;
import com.phegon.foodapp.order.repository.OrderItemRepository;
    import com.phegon.foodapp.order.repository.OrderRepository;
    import com.phegon.foodapp.response.Response;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final CartService cartService;
    private final CartRepository cartRepository;


    @Value("${base.payment.link}")
    private String basePaymentLink;




            @Override
            @Transactional
            public Response<?> placeOrderFromCart() {
                log.info("dentro de Place order from cart");

                User customer = userService.getCurrentLoggedInUser();

                String deliveryAddress = customer.getAddress();

                if(deliveryAddress == null) {
                    throw new NotFoundException("Delivery address not found for the user");
                }

                Cart cart = cartRepository.findByUser_Id(customer.getId())
                        .orElseThrow(() -> new NotFoundException("carrito no encontrado para el usuario"));
                List<CartItem> cartItems = cart.getCartItems();
                if(cartItems==null || cartItems.isEmpty()) throw new BadRequestException("carritop vacio");

                List<OrderItem> orderItems = new ArrayList<>();
                BigDecimal totalAmount= BigDecimal.ZERO;

                for(CartItem cartItem : cartItems) {
                    OrderItem orderItem = OrderItem.builder()
                            .menu(cartItem.getMenu())
                            .quantity(cartItem.getQuantity())
                            .pricePerUnit(cartItem.getPricePerUnit())
                            .subtotal(cartItem.getSubtotal())
                            .build();
                    orderItems.add(orderItem);
                    totalAmount = totalAmount.add(cartItem.getSubtotal());

                }

                Order order =Order.builder()
                        .user(customer)
                        .orderItems(orderItems)
                        .orderDate(LocalDateTime.now())
                        .totalAmount(totalAmount)
                        .orderStatus(OrderStatus.INITIALIZED)
                        .paymentStatus(PaymentStatus.PENDING)
                        .build();

                Order savedOrder = orderRepository.save(order);
                orderItems.forEach(orderItem -> orderItem.setOrder(savedOrder));
                orderItemRepository.saveAll(orderItems);

                cartService.clearShoppingCart();

                OrderDTO orderDTO =modelMapper.map(savedOrder, OrderDTO.class);


                sendOrderConfirmationEmail(customer,orderDTO);


                return Response.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("tu orden ha sido recibida , enviaremos un link seguro a tu email , porfavor porceda con el pago  para confrimar su orden")
                        .build();



            }





    @Override
    public Response<OrderDTO> getOrderById(Long id) {
        log.info("dentro de getOrderById");
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));

        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        return Response.<OrderDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("orden recuperada con exito")
                .data(orderDTO)
                .build();
    }

    @Override
    public Response<Page<OrderDTO>> getAllOrders(OrderStatus orderStatus, int page, int size) {
        log.info("dentro de getAllOrders");
        Pageable pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> orderPage;

        if(orderStatus!=null){
            orderPage = orderRepository.findByOrderStatus(orderStatus, pageable);
        }else {
            orderPage = orderRepository.findAll(pageable);
        }

        Page<OrderDTO> orderDTOPage = orderPage.map(order->{
            OrderDTO dto = modelMapper.map(order, OrderDTO.class);
            dto.getOrderItems().forEach(orderItemDTO -> orderItemDTO.getMenu().setReviews(null));
            return dto;

                });

        return Response.<Page<OrderDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("ordenes recuperadas con exito")
                .data(orderDTOPage)
                .build();
    }

    @Override
    public Response<List<OrderDTO>> getOrdersOfUser() {
        log.info("dentro de getOrdersOfUser");

        User customer = userService.getCurrentLoggedInUser();
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(customer);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> modelMapper.map(order,OrderDTO.class))
                .toList();

        orderDTOs.forEach(orderItem -> {
            orderItem.setUser(null);
            orderItem.getOrderItems().forEach(item -> item.getMenu().setReviews(null)   );
        });

        return Response.<List<OrderDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("ordenes recuperadas con exito")
                .data(orderDTOs)
                .build();

    }

    @Override
    public Response<OrderItemDTO> getOrderItemById(Long orderItemId) {
        log.info("dentro de getOrderItemById");

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("order item not found"));

        OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
        orderItemDTO.setMenu(modelMapper.map(orderItem.getMenu(), MenuDTO.class));

        return Response.<OrderItemDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("orderItem recuperado con exito")
                .data(orderItemDTO)
                .build();

    }

    @Override
    public Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO) {
        log.info("dentro de updateOrderStatus ");
        Order order= orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new NotFoundException("order not found"));

        OrderStatus orderStatus = orderDTO.getOrderStatus();
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        return Response.<OrderDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("status de orden actualizado")
                .build();
    }

    @Override
    public Response<Long> countUniqueCustomers() {
        log.info("dentro de countUniqueCustomers");
        long uniqueCustomerCount = orderRepository.countDistinctUsers();

        return  Response.<Long>builder()
                .statusCode(HttpStatus.OK.value())
                .message("unique customer count recuperadop con exito")
                .data(uniqueCustomerCount)
                .build();
    }

    private void sendOrderConfirmationEmail(User customer, OrderDTO orderDTO) {
        String subject = "Order Confirmation - Orden # "+orderDTO.getId();

        //thymeleaf
        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName", customer.getName());
        context.setVariable("orderId", String.valueOf(orderDTO.getId()));
        context.setVariable("orderDate", orderDTO.getOrderDate().toString());
        context.setVariable("totalAmount", orderDTO.getTotalAmount().toString());

        String deliveryAddress = orderDTO.getUser().getAddress();

        context.setVariable("deliveryAddress", deliveryAddress);
        context.setVariable("currentYear",java.time.Year.now());

        StringBuilder orderItemsHtml=new StringBuilder();

        for(OrderItemDTO item: orderDTO.getOrderItems()) {
            orderItemsHtml.append("<div class=\"order-item\">")
                    .append("<p>").append(item.getMenu().getName()).append(" x").append(item.getQuantity()).append("</p>")
                    .append("<p> $").append(item.getSubtotal()).append("</p>")
                    .append("</div>");

        }
        context.setVariable("orderItemHtml", orderItemsHtml.toString());
        context.setVariable("totalItems",orderDTO.getOrderItems().size());

        String paymentLink = basePaymentLink+orderDTO.getId()+"&amount"+orderDTO.getTotalAmount();
        context.setVariable("paymentLink", paymentLink);

        String emailBody = templateEngine.process("order-confirmation", context);

        notificationService.sendEmail(NotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject(subject)
                .body(emailBody)
                .isHtml(true)
                .build());


    }

}
