package com.example.demo.repository;
import com.example.demo.model.Chatroom;
import com.example.demo.model.Participant;
import com.example.demo.model.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Integer> {
    Participant findById(String id);

    List<Participant> findAllByRoom(Chatroom room);
    List<Participant> findAllByParticipant(UserDTO participant);
}
