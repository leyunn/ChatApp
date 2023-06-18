package com.example.demo.controller;

import com.example.demo.model.Chatroom;
import com.example.demo.model.User;
import com.example.demo.model.UserDTO;
import com.example.demo.response.ErrorResponse;
import com.example.demo.response.util;
import com.example.demo.service.AccountService;
import com.example.demo.service.ChatService;
import com.example.demo.service.FriendService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
public class FriendController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private ChatService chatService;

    @GetMapping("/view/{id}")
    public ResponseEntity<?> getInfo(@PathVariable String id, @RequestHeader("Authorization") String token) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        User user = accountService.findUser(id);
        List<UserDTO> friends = friendService.getFriends(user);
        return ResponseEntity.status(200).body(friends);
    }

    @PostMapping("/add/{id}")
    public ResponseEntity<?> addFriend(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestBody FriendForm friendForm) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        if(!accountService.checkUserExist(friendForm.getId())){
            return ResponseEntity.status(404).body(new ErrorResponse("user not exist"));
        }
        User user = accountService.findUser(id);
        User friend = accountService.findUser(friendForm.getId());
        if(friendService.isYourFriend(user, friend)){
            return ResponseEntity.status(409).body(new ErrorResponse("is your friend"));
        }
        friendService.addFriend(user, friend);

        return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<?> searchFriend(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestBody FriendForm friendForm) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        if(!accountService.checkUserExist(friendForm.getId())){
            return ResponseEntity.status(404).body(new ErrorResponse("user not exist"));
        }
        User friend = accountService.findUser(friendForm.getId());

        return ResponseEntity.status(200).body(friend.toDTO());
    }

    @PostMapping("/message/{id}")
    public ResponseEntity<?> sendMessage(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestBody MessageFriendForm messageForm) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        User user = accountService.findUser(id);
        User friend = accountService.findUser(messageForm.getFriendId());
        if(!friendService.isYourFriend(user, friend)){
            return ResponseEntity.status(403).body(new ErrorResponse("not your friend"));
        }
        Chatroom ourRoom = chatService.findOurChatroom(user, friend);
        if(ourRoom==null){
            ourRoom = chatService.createPrivateChatroom(user, friend);
        }
        chatService.sendMessage(user, ourRoom, messageForm.getContent());
        return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
    }

    @Data
    @NoArgsConstructor
    public static class FriendForm{
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class MessageFriendForm{
        private String friendId;
        private String content;
    }
}


