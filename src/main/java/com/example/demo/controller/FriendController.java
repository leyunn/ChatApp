package com.example.demo.controller;

import com.example.demo.model.Chatroom;
import com.example.demo.model.Message;
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

/**
 * The FriendController class handles HTTP requests related to friend functionality.
 * It interacts with the AccountService, FriendService, and ChatService to perform friend-related operations.
 */
@RestController
@RequestMapping("/friend")
public class FriendController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private ChatService chatService;

    /**
     * Retrieves information about the friends of a user.
     *
     * @param id    The user ID.
     * @param token The authorization token.
     * @return The ResponseEntity containing the list of friends or an error response.
     */
    @GetMapping("/view/{id}")
    public ResponseEntity<?> getInfo(@PathVariable String id, @RequestHeader("Authorization") String token) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        User user = accountService.findUser(id);
        List<UserDTO> friends = friendService.getFriends(user);
        return ResponseEntity.status(200).body(friends);
    }

    /**
     * Adds a friend to a user's friend list.
     *
     * @param id         The user ID.
     * @param token      The authorization token.
     * @param friendForm The friend form containing the friend's user ID.
     * @return The ResponseEntity indicating the success or failure of adding the friend.
     */
    @PostMapping("/add/{id}")
    public ResponseEntity<?> addFriend(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestBody FriendForm friendForm) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        if(!accountService.checkUserExist(friendForm.getId())){
            return ResponseEntity.status(404).body(new ErrorResponse("User does not exist"));
        }
        User user = accountService.findUser(id);
        User friend = accountService.findUser(friendForm.getId());
        if(friendService.isYourFriend(user, friend)){
            return ResponseEntity.status(409).body(new ErrorResponse("User is already your friend"));
        }
        friendService.addFriend(user, friend);

        return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
    }

    /**
     * Searches for a friend based on the friend's user ID.
     *
     * @param id       The user ID.
     * @param token    The authorization token.
     * @param friendId The ID of the friend to search for.
     * @return The ResponseEntity containing the friend's information or an error response.
     */
    @GetMapping("/search/{id}")
    public ResponseEntity<?> searchFriend(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestParam String friendId) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        if(!accountService.checkUserExist(friendId)){
            return ResponseEntity.status(404).body(new ErrorResponse("User does not exist"));
        }
        User friend = accountService.findUser(friendId);

        return ResponseEntity.status(200).body(friend.toDTO());
    }

    /**
     * Retrieves the chat history between a user and a friend.
     *
     * @param id        The user ID.
     * @param token     The authorization token.
     * @param friendId  The ID of the friend.
     * @return The ResponseEntity containing the list of messages or an error response.
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity<?> viewChatHistory(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestParam String friendId) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        User user = accountService.findUser(id);
        User friend = accountService.findUser(friendId);
        Chatroom room = chatService.findOurChatroom(user, friend);
        if(room == null){
            return ResponseEntity.status(404).body(new ErrorResponse("Chatroom not found"));
        }
        List<Message> messages = chatService.getAllMessage(room);
        return ResponseEntity.status(200).body(messages);
    }

    /**
     * Sends a message from a user to a friend.
     *
     * @param id           The user ID.
     * @param token        The authorization token.
     * @param messageForm  The message form containing the friend's user ID and message content.
     * @return The ResponseEntity indicating the success or failure of sending the message.
     */
    @PostMapping("/message/{id}")
    public ResponseEntity<?> sendMessage(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestBody MessageFriendForm messageForm) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        User user = accountService.findUser(id);
        User friend = accountService.findUser(messageForm.getFriendId());
        if(!friendService.isYourFriend(user, friend)){
            return ResponseEntity.status(403).body(new ErrorResponse("Not your friend"));
        }
        Chatroom ourRoom = chatService.findOurChatroom(user, friend);
        if(ourRoom==null){
            ourRoom = chatService.createPrivateChatroom(user, friend);
        }
        chatService.sendMessage(user, ourRoom, messageForm.getContent());
        return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
    }

    /**
     * The FriendForm class represents the form data for adding a friend.
     * It contains the friend's user ID.
     */
    @Data
    @NoArgsConstructor
    public static class FriendForm{
        private String id;
    }

    /**
     * The MessageFriendForm class represents the form data for sending a message to a friend.
     * It contains the friend's user ID and message content.
     */
    @Data
    @NoArgsConstructor
    public static class MessageFriendForm{
        private String friendId;
        private String content;
    }
}
