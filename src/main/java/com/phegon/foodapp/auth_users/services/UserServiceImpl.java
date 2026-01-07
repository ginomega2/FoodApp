package com.phegon.foodapp.auth_users.services;

import com.phegon.foodapp.auth_users.dtos.UserDTO;
import com.phegon.foodapp.auth_users.entity.User;
import com.phegon.foodapp.auth_users.repository.UserRepository;
import com.phegon.foodapp.aws.AWSS3Service;
import com.phegon.foodapp.email_notification.dtos.NotificationDTO;
import com.phegon.foodapp.email_notification.services.NotificationService;
import com.phegon.foodapp.exceptions.BadRequestException;
import com.phegon.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private  final AWSS3Service awss3Service;


    @Override
    public User getCurrentLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        String email = "ginomega2@gmail.com";
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

    }

    @Override
    public Response<List<UserDTO>> getAllUsers() {
        log.info("getAllUsers");
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<UserDTO> userDTOS= modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());

        return Response.<List<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("todos los ususarios recuperados")
                .data(userDTOS)
                .build();

    }

    @Override
    public Response<UserDTO> getOwnAccountDetails() {

        log.info("getOwnAccountDetails");
        User user = getCurrentLoggedInUser();
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("ya esta hecho")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<?> updateOwnAccount(UserDTO userDTO) {
        log.info("updateOwnAccount");
        User user = getCurrentLoggedInUser();

        String profileUrl = user.getProfileUrl();
        MultipartFile imageFile = userDTO.getImageFile();

        if(imageFile != null &&  !imageFile.isEmpty() ){
            if(profileUrl != null && !profileUrl.isEmpty()){
                String keyName = profileUrl.substring(profileUrl.lastIndexOf("/")+1);
                awss3Service.deleteFile("profile/" +keyName);
                log.info("Delete old profile image file from s3");
            }
            String imageName = UUID.randomUUID().toString()+"_"+imageFile.getOriginalFilename();
            URL newImageUrl= awss3Service.uploadFile("profile/"+imageName,imageFile);
            user.setProfileUrl(newImageUrl.toString());

        }
        if(userDTO.getName()!=null) user.setName(userDTO.getName());
        if(userDTO.getPhoneNumber()!=null) user.setPhoneNumber(userDTO.getPhoneNumber());
        if(userDTO.getAddress()!=null) user.setAddress(userDTO.getAddress());

        //if(userDTO.getPassword()!=null) user.setPassword(userDTO.getPassword());
        user.setUpdatedAt(LocalDateTime.now());

        if(userDTO.getEmail() !=null && !userDTO.getEmail().equals(user.getEmail())) {
            if(userRepository.existsByEmail(userDTO.getEmail())){
                throw new BadRequestException("Email ya existe");
            }
            user.setEmail(user.getEmail());
        }

        if (userDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        userRepository.save(user);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("cunenta actualizada")
                .build();


    }

    @Override
    public Response<?> deactivateOwnAccount() {
        log.info("deactivateAccount");

        User user = getCurrentLoggedInUser();
        user.setActive(false);
        userRepository.save(user);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("cuenta desactivada")
                .body("tu cuenta ha sido desactivada")
                .build();

        notificationService.sendEmail(notificationDTO);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("desactivacion de cuenta realizada")
                .build();



    }
}
