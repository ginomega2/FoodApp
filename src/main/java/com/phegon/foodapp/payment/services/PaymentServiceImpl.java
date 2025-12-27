package com.phegon.foodapp.payment.services;

import com.phegon.foodapp.email_notification.dtos.NotificationDTO;
import com.phegon.foodapp.email_notification.services.NotificationService;
import com.phegon.foodapp.enums.OrderStatus;
import com.phegon.foodapp.enums.PaymentGateway;
import com.phegon.foodapp.enums.PaymentStatus;
import com.phegon.foodapp.exceptions.BadRequestException;
import com.phegon.foodapp.exceptions.NotFoundException;
import com.phegon.foodapp.order.entity.Order;
import com.phegon.foodapp.order.repository.OrderRepository;
import com.phegon.foodapp.payment.dtos.PaymentDTO;
import com.phegon.foodapp.payment.entity.Payment;
import com.phegon.foodapp.payment.repository.PaymentRepository;
import com.phegon.foodapp.response.Response;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final TemplateEngine templateEngine;
    private final ModelMapper modelMapper;


    @Value("${stripe.api.secret.key}")
    private String secretKey;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    @Override
    public Response<?> initializePayment(PaymentDTO paymentRequest) {
        log.info("dentro de initializePayment");
        Stripe.apiKey=secretKey;

        Long orderId= paymentRequest.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException("orden no encontyrada"));

        if(order.getPaymentStatus()== PaymentStatus.COMPLETED){
            throw new BadRequestException("orden ya pagada completada");

        }
        if(paymentRequest.getAmount()== null){
            throw new BadRequestException("orden es null");
        }
        if(order.getTotalAmount().compareTo(paymentRequest.getAmount())!=0){
            throw new BadRequestException("paymen amount nota tally");


        }

        try {
            PaymentIntentCreateParams params =PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency("usd")
                    .putMetadata("orderId",String.valueOf(orderId))
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            String uniqueTransactionId= intent.getClientSecret();

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("logrado")
                    .data(uniqueTransactionId)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void updatePaymentForOrder(PaymentDTO paymentDTO) {

        log.info("dentro de updatePaymentForOrder");

        Long orderId= paymentDTO.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException("orden no encontyrada"));

        Payment payment = new Payment();
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setAmount(paymentDTO.getAmount());
        payment.setTransactionId(paymentDTO.getTransactionId());
        payment.setPaymentStatus(paymentDTO.isSuccess() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setOrder(order);
        if(!paymentDTO.isSuccess()){
            payment.setFailureReason(paymentDTO.getFailureReason());
        }
        paymentRepository.save(payment);

        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName",order.getUser().getName());
        context.setVariable("orderId",order.getId());
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("amount", "$"+ paymentDTO.getAmount());

        if(paymentDTO.isSuccess()){
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            context.setVariable("transactionId",paymentDTO.getTransactionId());
            context.setVariable("paymentDate",LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
            context.setVariable("frontendBaseUrl",this.frontendBaseUrl);
            String emailBody = templateEngine.process("payment-success", context);

            notificationService.sendEmail(NotificationDTO.builder()
                    .recipient(order.getUser().getEmail())
                    .subject("Pago realizado - orden # "+order.getId())
                    .body(emailBody)
                    .isHtml(true)
                    .build());


        }else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            context.setVariable("failureReason",paymentDTO.getFailureReason());
            String emailBody= templateEngine.process("payment-failed", context);

            notificationService.sendEmail(NotificationDTO.builder()
                            .recipient(order.getUser().getEmail())
                            .subject("fallo en Pago realizado - orden # "+order.getId())
                            .body(emailBody)
                            .isHtml(true)
                    .build());



        }


    }

    @Override
    public Response<List<PaymentDTO>> getAllPayments() {
        log.info("dentro de getAllPayments");
        List<Payment> paymentList = paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<PaymentDTO>paymentDTOS= modelMapper.map(paymentList,new TypeToken<List<PaymentDTO>>(){}.getType());
        paymentDTOS.forEach(item ->{
            item.setOrder(null);
            item.setUser(null);
        });


        return Response.<List<PaymentDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("pago recuperado con exito")
                .data(paymentDTOS)
                .build();
    }

    @Override
    public Response<PaymentDTO> getPaymentById(Long paymentId) {
        log.info("denro de getPaymentById");
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("pago no encontyrado"));
        PaymentDTO paymentDTOS=modelMapper.map(payment,PaymentDTO.class);

        paymentDTOS.getUser().setRoles(null);
        paymentDTOS.getOrder().setUser(null);
        paymentDTOS.getOrder().getOrderItems().forEach(item ->{
            item.getMenu().setReviews(null);
        } );



        return Response.<PaymentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("pago recuperado con exito por id")
                .data(paymentDTOS)
                .build();
    }
}

