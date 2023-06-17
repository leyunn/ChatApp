package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.ChatroomRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ChatService {
    @Autowired
    ChatroomRepository chatroomRepo;

    @Autowired
    ParticipantRepository participantRepo;

    @Autowired
    MessageRepository messageRepo;

    public List<Chatroom> viewAllChatroom(User user){
        //group
        List<Participant> groupRooms = participantRepo.findAllByParticipant(user.toDTO());
        List<Chatroom> chatrooms = new ArrayList<>();
        for (Participant participant: groupRooms) {
            chatrooms.add(participant.getRoom());
        }
        //private
        List<Chatroom> privateRooms = chatroomRepo.findAllByParticipant1OrParticipant2(user.toDTO(), user.toDTO());
        for (Chatroom room: privateRooms) {
            if(!room.isGroup()){
                UserDTO participant1 = room.getParticipant1();
                UserDTO participant2 = room.getParticipant2();
                if(!participant1.getId().equals(user.getId())){
                    room.setRoomName(participant1.getName());
                }else{
                    room.setRoomName(participant2.getName());
                }
            }
            chatrooms.add(room);
        }
        Collections.sort(chatrooms, Comparator.comparing(Chatroom::getLastModified).reversed());
        return chatrooms;
    }

    public Chatroom createPrivateChatroom(User user1, User user2){
        Chatroom room = new Chatroom();
        room.setGroup(false);
        room.setParticipant1(user1.toDTO());
        room.setParticipant2(user2.toDTO());
        room.setLastModified(LocalDateTime.now());
        chatroomRepo.save(room);
        return room;
    }
    public Chatroom findChatroom(Long id){
        return chatroomRepo.findById(id);
    }

    public Chatroom findOurChatroom(User user1, User user2){
        Chatroom room = chatroomRepo.findByParticipant1AndParticipant2(user1.toDTO(), user2.toDTO());
        if(room == null){
            return chatroomRepo.findByParticipant1AndParticipant2(user2.toDTO(), user1.toDTO());
        }
        return  room;
    }

    public List<Message> getAllMessage(Chatroom room){
        return messageRepo.findAllByRoom(room);
    }
    //must make sure room exist first
    public void sendMessage(User sender, Chatroom room, String content){
        Message message = new Message();
        message.setContent(content);
        message.setRoom(room);
        message.setSender(sender.toDTO());
        message.setTime(LocalDateTime.now());
        messageRepo.save(message);
        room.setLastModified(LocalDateTime.now());
        chatroomRepo.save(room);
    }

    public boolean inChatroom(Chatroom room, User user){
        if(!room.isGroup()){
            return room.getParticipant1().getId().equals(user.getId()) || room.getParticipant2().getId().equals(user.getId());
        }else{
            List<Participant> groupRooms = participantRepo.findAllByRoom(room);
            for (Participant participant: groupRooms) {
                if(participant.getParticipant().getId() == user.getId()){
                    return true;
                }
            }
        }
        return  false;
    }

    public List<UserDTO> getAllParticipantExceptSender(Chatroom room, User sender){
        List<UserDTO> participants = new ArrayList<>();
        if(!room.isGroup()){
            UserDTO participant1 = room.getParticipant1();
            if(!participant1.getId().equals(sender.getId())) participants.add(participant1);
            UserDTO participant2 = room.getParticipant2();
            if(!participant2.getId().equals(sender.getId())) participants.add(participant2);
        }else{
            List<Participant> groupRooms = participantRepo.findAllByRoom(room);
            for (Participant grouper: groupRooms) {
                UserDTO participant = grouper.getParticipant();
                if(!participant.getId().equals(sender.getId())){
                    participants.add(participant);
                }
            }
        }
        return  participants;
    }

    public List<UserDTO> getAllParticipant(Chatroom room){
        List<UserDTO> participants = new ArrayList<>();
        if(!room.isGroup()){
            UserDTO participant1 = room.getParticipant1();
            participants.add(participant1);
            UserDTO participant2 = room.getParticipant2();
            participants.add(participant2);
        }else{
            List<Participant> groupRooms = participantRepo.findAllByRoom(room);
            for (Participant grouper: groupRooms) {
                UserDTO participant = grouper.getParticipant();
                participants.add(participant);
            }
        }
        return  participants;
    }













}
