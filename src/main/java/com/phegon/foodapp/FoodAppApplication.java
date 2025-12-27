package com.phegon.foodapp;

import com.phegon.foodapp.email_notification.dtos.NotificationDTO;
import com.phegon.foodapp.email_notification.services.NotificationService;
import com.phegon.foodapp.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
public class FoodAppApplication {

    private final NotificationService notificationService;




    public static void main(String[] args) {
        SpringApplication.run(FoodAppApplication.class, args);

    }
    @Bean
    CommandLineRunner runner() {
        return args -> {
            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .recipient("mrpisbackhere@gmail.com")
                    .subject("HOLA MI AMOR ESTE CORRERO LO HICE EN JAVA")
                    .body("ES UNA PRUEBA DE MI ESTUDIO")
                    .type(NotificationType.EMAIL)
                    .build();

            notificationService.sendEmail(notificationDTO);
        };

}



}
