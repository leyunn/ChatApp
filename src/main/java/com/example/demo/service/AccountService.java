package com.example.demo.service;

import com.example.demo.controller.AccountController;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The AccountService class provides various operations related to user accounts.
 * It interacts with the UserRepository to access and manipulate user data.
 */
@Service
public class AccountService {
    private final Map<String, String> userMap = new HashMap<>();

    @Autowired
    private UserRepository userRepo;

    /**
     * Creates a new user by saving the user object in the UserRepository.
     *
     * @param user The user object to create.
     */
    public void createUser(User user){
        userRepo.save(user);
    }

    /**
     * Checks if a user with the specified ID exists in the UserRepository.
     *
     * @param id The ID of the user to check.
     * @return true if the user exists, false otherwise.
     */
    public boolean checkUserExist(String id){
        return userRepo.existsById(id);
    }

    /**
     * Retrieves a user with the specified ID from the UserRepository.
     *
     * @param id The ID of the user to retrieve.
     * @return The User object if found, null otherwise.
     */
    public User findUser(String id){
        return userRepo.findById(id);
    }

    /**
     * Verifies if the provided password matches the password of the user.
     *
     * @param user     The User object to verify.
     * @param password The password to verify.
     * @return true if the password is verified, false otherwise.
     */
    public boolean verifyUser(User user, String password){
        //TODO: Add encoder
        return user.getPassword().equals(password);
    }

    /**
     * Checks if a user is authenticated based on the provided user ID and token.
     *
     * @param userID The user ID.
     * @param token  The authentication token.
     * @return true if the user is authenticated, false otherwise.
     */
    public boolean isAuthenticated(String userID, String token) {
        // Check if the provided user number and token match the stored values
        String storedToken = userMap.get(userID);
        return storedToken != null && storedToken.equals(token);
    }

    /**
     * Retrieves the notification topic for a user based on the user ID.
     *
     * @param userId The user ID.
     * @return The notification topic for the user, or null if not found.
     */
    public String getNotificationTopic(String userId){
        String storedToken = userMap.get(userId);
        if(storedToken == null) return null;
        return "/topic/notifications/" + storedToken;
    }

    /**
     * Logs in a user and returns an authentication token.
     * If the user is already logged in, the existing token is returned.
     *
     * @param userID The user ID.
     * @return The authentication token.
     */
    public String login(String userID) {
        String token = userMap.get(userID);
        if(token == null){
            token = UUID.randomUUID().toString();
            userMap.put(userID, token);
        }
        return token;
    }

    /**
     * Logs out a user by removing the authentication token.
     *
     * @param userID The user ID.
     * @return true if the user was successfully logged out, false otherwise.
     */
    public boolean logout(String userID) {
        String token = userMap.get(userID);
        if(token == null){
            return false;
        }
        userMap.remove(userID);
        return true;
    }

    /**
     * Sets the API key for a user by updating the user object in the UserRepository.
     *
     * @param user The user object to update.
     * @param key  The API key to set.
     */
    public void setAPIKey(User user, String key){
        user.setAPIKey(key);
        userRepo.save(user);
    }
}
