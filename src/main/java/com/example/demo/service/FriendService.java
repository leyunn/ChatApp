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

/**
 * Service class that handles operations related to user friendships.
 */
@Service
public class FriendService {
    @Autowired
    private FriendShipRepository friendShipRepo;

    /**
     * Retrieves a list of friends for a given user.
     *
     * @param user The user.
     * @return A list of UserDTO representing the friends of the user.
     */
    public List<UserDTO> getFriends(User user){
        List<Friendship> friendships = friendShipRepo.findAllByUser(user.toDTO());
        List<UserDTO> friends = new ArrayList<>();
        for (Friendship friendship : friendships) {
            friends.add(friendship.getFriend());
        }
        return friends;
    }

    /**
     * Adds a friend for a given user.
     *
     * @param user   The user.
     * @param friend The friend to be added.
     */
    public void addFriend(User user, User friend){
        Friendship friendship = new Friendship();
        friendship.setUser(user.toDTO());
        friendship.setFriend(friend.toDTO());
        friendShipRepo.save(friendship);
    }

    /**
     * Checks if a user is a friend of another user.
     *
     * @param user   The user.
     * @param friend The potential friend.
     * @return true if the user is a friend of the friend, false otherwise.
     */
    public boolean isYourFriend(User user, User friend){
        return friendShipRepo.findByUserAndFriend(user.toDTO(), friend.toDTO()) != null;
    }
}
