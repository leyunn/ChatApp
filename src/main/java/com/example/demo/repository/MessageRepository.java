package com.example.demo.repository;
import com.example.demo.model.Chatroom;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findAllByRoom(Chatroom room);
}
