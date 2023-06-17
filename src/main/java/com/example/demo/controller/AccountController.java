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

    @GetMapping("/info/{id}")
    public ResponseEntity<?> getInfo(@PathVariable String id, @RequestHeader("Authorization") String token) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        User user = accountService.findUser(id);
        return ResponseEntity.status(200).body(user);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        if(!user.isValid()){
            return ResponseEntity.status(415).body(new ErrorResponse("format error"));
        }
        if(accountService.checkUserExist(user.getId())){
            return ResponseEntity.status(409).body(new ErrorResponse("user exist"));
        }
        accountService.createUser(user);
        return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginForm loginForm, HttpServletRequest request) {
        User user = accountService.findUser(loginForm.getId());
        if(user==null || !accountService.verifyUser(user, loginForm.getPassword())){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        String token = accountService.login(user.getId());
        return ResponseEntity.status(200).body(new UserTokenResponse(user.getId(), token));
    }

    @PostMapping("/logout/{id}")
    public ResponseEntity<?> logout(@PathVariable String id, @RequestHeader("Authorization") String token) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }

        if(accountService.logout(id)){
            return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
        }else{
            return ResponseEntity.status(403).body(new ErrorResponse("not logged in"));
        }

    }

    @PostMapping("/key/{id}")
    public ResponseEntity<?> setAPIKey(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestBody ChatController.KeyForm keyForm) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        User user = accountService.findUser(id);
        accountService.setAPIKey(user, keyForm.getKey());
        return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
    }


    @Data
    @NoArgsConstructor
    public static class LoginForm{
        private String id;
        private String password;
    }
}
