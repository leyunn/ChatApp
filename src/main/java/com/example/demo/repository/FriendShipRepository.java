package com.example.demo.repository;
import com.example.demo.model.Friendship;
import com.example.demo.model.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendShipRepository extends JpaRepository<Friendship, Integer> {
    List<Friendship> findAllByUser(UserDTO user);

    Friendship findByUserAndFriend(UserDTO user, UserDTO friend);
}
