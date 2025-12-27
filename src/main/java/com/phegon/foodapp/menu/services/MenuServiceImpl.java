package com.phegon.foodapp.menu.services;

import com.phegon.foodapp.aws.AWSS3Service;
import com.phegon.foodapp.category.entity.Category;
import com.phegon.foodapp.category.repository.CategoryRepository;
import com.phegon.foodapp.exceptions.BadRequestException;
import com.phegon.foodapp.exceptions.NotFoundException;
import com.phegon.foodapp.menu.dtos.MenuDTO;
import com.phegon.foodapp.menu.entity.Menu;
import com.phegon.foodapp.menu.repository.MenuRepository;
import com.phegon.foodapp.response.Response;
import com.phegon.foodapp.review.dtos.ReviewDTO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final AWSS3Service awss3Service;

    @Override
    public Response<MenuDTO> createMenu(MenuDTO menuDTO) {
        log.info("dentro de createMenu ");
        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(()-> new NotFoundException("Category not found"));

        String imageUrl = null;
        MultipartFile imageFile = menuDTO.getImageFile();
        if (imageFile == null || imageFile.isEmpty() ) {
            throw new BadRequestException("Image file is empty");
        }
        String imageName = UUID.randomUUID()+"_"+imageFile.getOriginalFilename();
        URL s3Url = awss3Service.uploadFile("menus/"+imageName, imageFile);
        imageUrl = s3Url.toString();

        Menu menu = Menu.builder()
                .name(menuDTO.getName())
                .description(menuDTO.getDescription())
                .price(menuDTO.getPrice())
                .imageUrl(imageUrl)
                .category(category)
                .build();

        Menu savedMenu = menuRepository.save(menu);

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("menu crado ")
                .data(modelMapper.map(savedMenu, MenuDTO.class))
                .build();


    }

    @Override
    public Response<MenuDTO> updateMenu(MenuDTO menuDTO) {
        log.info("dentro de updateMenu");
        Menu existingMenu = menuRepository.findById(menuDTO.getId())
                .orElseThrow(()-> new NotFoundException("Menu not found"));

        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(()-> new NotFoundException("Category not found"));

        System.out.println("***************** entrada UPDATE MENU*****************************************");
        System.out.println("getname "+menuDTO.getName());
        System.out.println("getDescription()"+menuDTO.getDescription());
        System.out.println(".getPrice() "+menuDTO.getPrice());
        System.out.println("**********************************************************");


        String imageUrl = existingMenu.getImageUrl();
        MultipartFile imageFile = menuDTO.getImageFile();


        if(imageFile != null &&  !imageFile.isEmpty() ){
            if(imageUrl != null && !imageUrl.isEmpty()){
                String keyName = imageUrl.substring(imageUrl.lastIndexOf("/")+1);
                awss3Service.deleteFile("menus/" +keyName);
                log.info("Delete old menu image file from s3");
            }
            String imageName = UUID.randomUUID().toString()+"_"+imageFile.getOriginalFilename();
            URL newImageUrl= awss3Service.uploadFile("menus/"+imageName,imageFile);

            imageUrl = newImageUrl.toString();


        }

        System.out.println("*****************salida UPDATE MENU*****************************************");
        System.out.println("getname "+menuDTO.getName());
        System.out.println("getDescription()"+menuDTO.getDescription());
        System.out.println(".getPrice() "+menuDTO.getPrice());
        System.out.println("**********************************************************");

        if(menuDTO.getName()!=null && !menuDTO.getName().isBlank()) existingMenu.setName(menuDTO.getName());
        if(menuDTO.getDescription()!=null && !menuDTO.getDescription().isBlank()) existingMenu.setDescription(menuDTO.getDescription());
        if(menuDTO.getPrice()!=null) existingMenu.setPrice(menuDTO.getPrice());
        existingMenu.setCategory(category);
        existingMenu.setImageUrl(imageUrl);
        System.out.println("*****************guardadfo  UPDATE MENU*****************************************");
        System.out.println("getname "+menuDTO.getName());
        System.out.println("getDescription()"+menuDTO.getDescription());
        System.out.println(".getPrice() "+menuDTO.getPrice());
        System.out.println("**********************************************************");
        Menu updatedMenu = menuRepository.save(existingMenu);

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("menu atualizado ")
                .data(modelMapper.map(updatedMenu, MenuDTO.class))
                .build();



    }

    @Override
    public Response<MenuDTO> getMenuById(Long id) {
        log.info("dentro de ");

        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Menu not found"));

        MenuDTO menuDTO = modelMapper.map(existingMenu, MenuDTO.class);

        if(menuDTO.getReviews()!=null){
            menuDTO.getReviews().sort(Comparator.comparing(ReviewDTO::getId).reversed());
        }
        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("menun localizado y leiod")
                .data(menuDTO)
                .build();
    }

    @Override
    public Response<?> deleteMenu(Long id) {
        log.info("dentro de ");

        Menu menuToDelete = menuRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Menu not found"));

        String imageUrl = menuToDelete.getImageUrl();
        if(imageUrl != null && !imageUrl.isEmpty()){
            String keyName = imageUrl.substring(imageUrl.lastIndexOf("/")+1);
            awss3Service.deleteFile("menus/" +keyName);
            log.info("Delete old menu image file from s3");

        }
        menuRepository.deleteById(id);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("menu deletado ")
                .build();

    }

    @Override
    public Response<List<MenuDTO>> getMenus(Long categoryId, String search) {
        log.info("dentro de ");
        Specification<Menu> spec = buildSpecification(categoryId,search);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        List<Menu> menuList = menuRepository.findAll(spec,sort);

        List<MenuDTO> menuDTOS = menuList.stream()
                .map(menu -> modelMapper.map(menu,MenuDTO.class))
                .toList();

        return Response.<List<MenuDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(menuDTOS)
                .build();

    }

    private Specification<Menu> buildSpecification(Long categoryId, String search) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(categoryId!=null){
                predicates.add(cb.equal(
                        root.get("category").get("id"),
                        categoryId
                ));
            }
            if(search!=null && !search.isBlank()){
                String searchTerm  = "%"+search.toLowerCase()+"%";
                predicates.add(cb.or(
                        cb.like(
                                cb.lower(root.get("name")),
                                searchTerm
                        ),
                        cb.like(
                                cb.lower(root.get("description")),
                                searchTerm
                        )
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));

        };

    }
}
