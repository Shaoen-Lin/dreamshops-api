package com.Shawn.dream_shops.service.user;

import com.Shawn.dream_shops.dto.UserDto;
import com.Shawn.dream_shops.exceptions.AlreadyExistsException;
import com.Shawn.dream_shops.exceptions.ResourceNotFoundException;
import com.Shawn.dream_shops.model.User;
import com.Shawn.dream_shops.reponse.CreateUserRequest;
import com.Shawn.dream_shops.reponse.UserUpdateRequest;
import com.Shawn.dream_shops.repository.UserRepository;
import com.Shawn.dream_shops.security.user.ShopUserDetails;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    @Autowired
    private final UserRepository userRepo;
    @Autowired
    private final ModelMapper modelMapper;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long userId) {
//        System.out.println(userId);
        return userRepo.findById(userId).orElseThrow( () -> new ResourceNotFoundException("User Not Found"));
    }

    @Override
    public User createUser(CreateUserRequest request) {
        return Optional.of(request) // wrap 住 request
                .filter(user -> !userRepo.existsByEmail(request.getEmail()))
                .map(req ->
                {
                    User user = new User();
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());
                    user.setEmail(request.getEmail());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));

                    return userRepo.save(user);
                }).orElseThrow(() -> new AlreadyExistsException("Oops" + request.getEmail() + "already exists!") );

    }

    @Override
    public User updateUser(UserUpdateRequest request, Long userId) {
        return userRepo.findById(userId)
                .map(exisitingUser -> {
                    exisitingUser.setFirstName(request.getFirstName());
                    exisitingUser.setLastName(request.getLastName());

                    return userRepo.save(exisitingUser);
                    // map 都要搭配 save
                }).orElseThrow( () -> new ResourceNotFoundException("User Not Found"));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepo.findById(userId)
                .ifPresentOrElse(userRepo :: delete,
                                () -> {throw new ResourceNotFoundException("User Not Found!");}
                );
    }

    @Override
    public UserDto convertUserToDto(User user)
    {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 修改這裡：加上 null 檢查
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
        return user;
    }

//    @Override
//    public User getAuthenticatedUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated() ||
//                authentication.getPrincipal().equals("anonymousUser")) {
//            throw new JwtException("User not authenticated"); // 會被 controller 捕捉到
//        }
//
//        ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();
//        return userRepo.findById(userDetails.getId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User not found with id: " + userDetails.getId()));
//    }

}
