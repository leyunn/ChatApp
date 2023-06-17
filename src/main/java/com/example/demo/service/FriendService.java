package com.example.demo.service;

import com.example.demo.model.Friendship;
import com.example.demo.model.User;
import com.example.demo.model.UserDTO;
import com.example.demo.repository.FriendShipRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FriendService {
    @Autowired
    private FriendShipRepository friendShipRepo;


    public List<UserDTO> getFriends(User user){
        List<Friendship> friendships = friendShipRepo.findAllByUser(user.toDTO());
        List<UserDTO> friends = new ArrayList<>();
        for (Friendship friendship : friendships) {
            friends.add(friendship.getFriend());
        }
        return friends;
    }

    public void addFriend(User user, User friend){
       Friendship friendship = new Friendship();
       friendship.setUser(user.toDTO());
       friendship.setFriend(friend.toDTO());
       friendShipRepo.save(friendship);
    }

    public boolean isYourFriend(User user, User friend){
        return friendShipRepo.findByUserAndFriend(user.toDTO(), friend.toDTO())!=null;
    }
}
