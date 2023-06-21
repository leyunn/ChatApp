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

/**
 * The ChatService class provides various operations related to chatrooms and messages.
 * It interacts with the ChatroomRepository, ParticipantRepository, and MessageRepository
 * to access and manipulate chatroom and message data.
 */
@Service
public class ChatService {
    @Autowired
    ChatroomRepository chatroomRepo;

    @Autowired
    ParticipantRepository participantRepo;

    @Autowired
    MessageRepository messageRepo;

    /**
     * Retrieves all chatrooms associated with a user.
     * This includes both group chatrooms and private chatrooms.
     *
     * @param user The user for whom to retrieve the chatrooms.
     * @return A list of chatrooms associated with the user.
     */
    public List<Chatroom> viewAllChatroom(User user){
        // Group chatrooms
        List<Participant> groupRooms = participantRepo.findAllByParticipant(user.toDTO());
        List<Chatroom> chatrooms = new ArrayList<>();
        for (Participant participant: groupRooms) {
            chatrooms.add(participant.getRoom());
        }
        // Private chatrooms
        List<Chatroom> privateRooms = chatroomRepo.findAllByParticipant1OrParticipant2(user.toDTO(), user.toDTO());
        for (Chatroom room: privateRooms) {
            if (!room.isGroup()) {
                UserDTO participant1 = room.getParticipant1();
                UserDTO participant2 = room.getParticipant2();
                if (!participant1.getId().equals(user.getId())) {
                    room.setRoomName(participant1.getName());
                } else {
                    room.setRoomName(participant2.getName());
                }
            }
            chatrooms.add(room);
        }
        Collections.sort(chatrooms, Comparator.comparing(Chatroom::getLastModified).reversed());
        return chatrooms;
    }

    /**
     * Creates a new private chatroom between two users.
     *
     * @param user1 The first user.
     * @param user2 The second user.
     * @return The created chatroom.
     */
    public Chatroom createPrivateChatroom(User user1, User user2){
        Chatroom room = new Chatroom();
        room.setGroup(false);
        room.setParticipant1(user1.toDTO());
        room.setParticipant2(user2.toDTO());
        room.setLastModified(LocalDateTime.now());
        chatroomRepo.save(room);
        return room;
    }

    /**
     * Finds a chatroom with the specified ID.
     *
     * @param id The ID of the chatroom to find.
     * @return The chatroom if found, null otherwise.
     */
    public Chatroom findChatroom(Long id){
        return chatroomRepo.findById(id);
    }

    /**
     * Finds the chatroom between two specified users.
     *
     * @param user1 The first user.
     * @param user2 The second user.
     * @return The chatroom between the two users if found, null otherwise.
     */
    public Chatroom findOurChatroom(User user1, User user2){
        Chatroom room = chatroomRepo.findByParticipant1AndParticipant2(user1.toDTO(), user2.toDTO());
        if(room == null){
            return chatroomRepo.findByParticipant1AndParticipant2(user2.toDTO(), user1.toDTO());
        }
        return  room;
    }

    /**
     * Retrieves all messages in a specified chatroom.
     *
     * @param room The chatroom.
     * @return A list of messages in the chatroom.
     */
    public List<Message> getAllMessage(Chatroom room){
        return messageRepo.findAllByRoom(room);
    }

    /**
     * Sends a message in a chatroom.
     * Updates the last modified time of the chatroom and the last message content.
     *
     * @param sender  The user sending the message.
     * @param room    The chatroom.
     * @param content The content of the message.
     */
    public void sendMessage(User sender, Chatroom room, String content){
        Message message = new Message();
        message.setContent(content);
        message.setRoom(room);
        message.setSender(sender.toDTO());
        message.setTime(LocalDateTime.now());
        messageRepo.save(message);
        room.setLastModified(LocalDateTime.now());
        room.setLastMessage(message.getSender().getName() + ": " + message.getContent());
        chatroomRepo.save(room);
    }

    /**
     * Checks if a user is a participant in a chatroom.
     *
     * @param room The chatroom.
     * @param user The user.
     * @return true if the user is a participant, false otherwise.
     */
    public boolean inChatroom(Chatroom room, User user){
        if (!room.isGroup()) {
            return room.getParticipant1().getId().equals(user.getId()) || room.getParticipant2().getId().equals(user.getId());
        } else {
            List<Participant> groupRooms = participantRepo.findAllByRoom(room);
            for (Participant participant: groupRooms) {
                if (participant.getParticipant().getId() == user.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves all participants in a chatroom except the sender.
     *
     * @param room   The chatroom.
     * @param sender The user who sent the request.
     * @return A list of participants in the chatroom except the sender.
     */
    public List<UserDTO> getAllParticipantExceptSender(Chatroom room, User sender){
        List<UserDTO> participants = new ArrayList<>();
        if (!room.isGroup()) {
            UserDTO participant1 = room.getParticipant1();
            if (!participant1.getId().equals(sender.getId())) {
                participants.add(participant1);
            }
            UserDTO participant2 = room.getParticipant2();
            if (!participant2.getId().equals(sender.getId())) {
                participants.add(participant2);
            }
        } else {
            List<Participant> groupRooms = participantRepo.findAllByRoom(room);
            for (Participant grouper: groupRooms) {
                UserDTO participant = grouper.getParticipant();
                if (!participant.getId().equals(sender.getId())) {
                    participants.add(participant);
                }
            }
        }
        return participants;
    }

    /**
     * Retrieves all participants in a chatroom.
     *
     * @param room The chatroom.
     * @return A list of participants in the chatroom.
     */
    public List<UserDTO> getAllParticipant(Chatroom room){
        List<UserDTO> participants = new ArrayList<>();
        if (!room.isGroup()) {
            UserDTO participant1 = room.getParticipant1();
            participants.add(participant1);
            UserDTO participant2 = room.getParticipant2();
            participants.add(participant2);
        } else {
            List<Participant> groupRooms = participantRepo.findAllByRoom(room);
            for (Participant grouper: groupRooms) {
                UserDTO participant = grouper.getParticipant();
                participants.add(participant);
            }
        }
        return participants;
    }
}
