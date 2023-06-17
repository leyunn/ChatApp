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
import com.example.demo.websocket.WebsocketSessionManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/room")
public class ChatController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private ChatService chatService;

    private WebsocketSessionManager websocketSessionManager;

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(WebsocketSessionManager sessionManager, SimpMessagingTemplate messagingTemplate) {
        this.websocketSessionManager = sessionManager;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/message/{id}")
    public ResponseEntity<?> sendMessageToRoom(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestBody MessageRoomForm messageForm) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        Chatroom room = chatService.findChatroom(messageForm.getRoomId());
        if(room == null){
            return ResponseEntity.status(404).body(new ErrorResponse("chatroom not found"));
        }
        User user = accountService.findUser(id);
        chatService.sendMessage(user, room, messageForm.getContent());
        //push notification
        List<UserDTO> onlineUsers = websocketSessionManager.filterOnlineUser(chatService.getAllParticipantExceptSender(room, user));
        for(UserDTO onlineUser: onlineUsers){
            String destination = accountService.getNotificationTopic(onlineUser.getId());
            System.out.println(destination);
            messagingTemplate.convertAndSend(destination, new Notification(room.getId(),user.getName(),messageForm.getContent())); // Send the notification
        }
        return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<?> viewChatHistory(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestParam Long roomId) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        Chatroom room = chatService.findChatroom(roomId);
        User user = accountService.findUser(id);
        if(room == null){
            return ResponseEntity.status(404).body(new ErrorResponse("chatroom not found"));
        }
        if(!chatService.inChatroom(room, user)){
            return ResponseEntity.status(403).body(new ErrorResponse("not your chatroom"));
        }
        List<Message> messages = chatService.getAllMessage(room);

        return ResponseEntity.status(200).body(messages);
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<?> viewChatRoom(@PathVariable String id, @RequestHeader("Authorization") String token) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        User user = accountService.findUser(id);
        List<Chatroom> chatrooms = chatService.viewAllChatroom(user);
        return ResponseEntity.status(200).body(chatrooms);
    }

    @GetMapping("/participants/{id}")
    public ResponseEntity<?> getParticipants(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestParam Long roomId) {
        if(!accountService.isAuthenticated(id, token)){
            return ResponseEntity.status(401).body(new ErrorResponse("authentication failed"));
        }
        Chatroom room = chatService.findChatroom(roomId);
        User user = accountService.findUser(id);
        if(room == null){
            return ResponseEntity.status(404).body(new ErrorResponse("chatroom not found"));
        }
        if(!chatService.inChatroom(room, user)){
            return ResponseEntity.status(403).body(new ErrorResponse("not your chatroom"));
        }
        List<UserDTO> participants = chatService.getAllParticipant(room);
        return ResponseEntity.status(200).body(participants);
    }



    @Data
    @NoArgsConstructor
    public static class FriendForm{
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class KeyForm{
        private String key;
    }

    @Data
    @NoArgsConstructor
    public static class MessageRoomForm{
        private Long roomId;

        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notification{
        private Long roomId;
        private String senderName;
        private String content;
    }

}


