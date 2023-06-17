package com.example.demo.service;
import com.example.demo.controller.AccountController;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AccountService {
    private final Map<String, String> userMap = new HashMap<>();
    @Autowired
    private UserRepository userRepo;
    public void createUser(User user){
        userRepo.save(user);
    }


    public boolean checkUserExist(String id){
        return userRepo.existsById(id);
    }

    public User findUser(String id){
        return userRepo.findById(id);
    }


    public boolean verifyUser(User user, String password){
        //TODO: add encoder
        return user.getPassword().equals(password);
    }

    public boolean isAuthenticated(String userID, String token) {
        // Check if the provided user number and token match the stored values
        String storedToken = userMap.get(userID);
        return storedToken != null && storedToken.equals(token);
    }

    public String getNotificationTopic(String userId){
        String storedToken = userMap.get(userId);
        if(storedToken ==null) return null;
        return "/topic/notifications/" + storedToken;
    }

    public String login(String userID) {
        String token = userMap.get(userID);
        if(token == null){
            token = UUID.randomUUID().toString();
            userMap.put(userID, token);
        }
        return token;
    }

    public boolean logout(String userID) {
        String token = userMap.get(userID);
        if(token == null){
            return false;
        }
        userMap.remove(userID);
        return true;
    }

    public void setAPIKey(User user, String key){
        user.setAPIKey(key);
        userRepo.save(user);
    }


}
