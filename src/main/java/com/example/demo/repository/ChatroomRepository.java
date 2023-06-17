package com.example.demo.repository;
import com.example.demo.model.Chatroom;
import com.example.demo.model.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatroomRepository extends JpaRepository<Chatroom, Integer> {
    Chatroom findById(Long id);

    List<Chatroom> findAllByParticipant1OrParticipant2(UserDTO user1, UserDTO user2);

    Chatroom findByParticipant1AndParticipant2(UserDTO user1, UserDTO user2);
}
