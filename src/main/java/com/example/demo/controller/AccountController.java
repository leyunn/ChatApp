/**
 * The AccountController class handles HTTP requests related to user accounts.
 * It interacts with the AccountService to perform account-related operations.
 */
package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.AccountService;
import com.example.demo.response.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private AccountService accountService;

    /**
     * Retrieves user information for the specified ID.
     *
     * @param id    The user ID.
     * @param token The authorization token.
     * @return The ResponseEntity containing the user information or an error response.
     */
    @GetMapping("/info/{id}")
    public ResponseEntity<?> getInfo(@PathVariable String id, @RequestHeader("Authorization") String token) {
        if (!accountService.isAuthenticated(id, token)) {
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        User user = accountService.findUser(id);
        return ResponseEntity.status(200).body(user);
    }

    /**
     * Registers a new user.
     *
     * @param user The user object containing the registration details.
     * @return The ResponseEntity indicating the success or failure of the registration.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        if (!user.isValid()) {
            return ResponseEntity.status(415).body(new ErrorResponse("Format error"));
        }
        if (accountService.checkUserExist(user.getId())) {
            return ResponseEntity.status(409).body(new ErrorResponse("User already exists"));
        }
        accountService.createUser(user);
        return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
    }

    /**
     * Logs in a user with the specified credentials.
     *
     * @param loginForm The login form containing the user ID and password.
     * @param request   The HttpServletRequest object.
     * @return The ResponseEntity containing the user token or an error response.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginForm loginForm, HttpServletRequest request) {
        User user = accountService.findUser(loginForm.getId());
        if (user == null || !accountService.verifyUser(user, loginForm.getPassword())) {
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        String token = accountService.login(user.getId());
        return ResponseEntity.status(200).body(new UserTokenResponse(user.getId(), token));
    }

    /**
     * Logs out a user with the specified ID and token.
     *
     * @param id    The user ID.
     * @param token The authorization token.
     * @return The ResponseEntity indicating the success or failure of the logout.
     */
    @PostMapping("/logout/{id}")
    public ResponseEntity<?> logout(@PathVariable String id, @RequestHeader("Authorization") String token) {
        if (!accountService.isAuthenticated(id, token)) {
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }

        if (accountService.logout(id)) {
            return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
        } else {
            return ResponseEntity.status(403).body(new ErrorResponse("Not logged in"));
        }
    }

    /**
     * Sets the API key for the specified user.
     *
     * @param id       The user ID.
     * @param token    The authorization token.
     * @param keyForm  The form containing the API key.
     * @return The ResponseEntity indicating the success or failure of setting the API key.
     */
    @PostMapping("/key/{id}")
    public ResponseEntity<?> setAPIKey(@PathVariable String id, @RequestHeader("Authorization") String token,
                                       @RequestBody ChatController.KeyForm keyForm) {
        if (!accountService.isAuthenticated(id, token)) {
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        User user = accountService.findUser(id);
        accountService.setAPIKey(user, keyForm.getKey());
        return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
    }

    /**
     * The LoginForm class represents the login form data.
     * It contains the user ID and password.
     */
    @Data
    @NoArgsConstructor
    public static class LoginForm {
        private String id;
        private String password;
    }
}
