package com.humber.Tasky.repository;

import com.humber.Tasky.model.FriendRequest;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// In FriendRequestRepository.java
@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {
    @Query("{'senderId': ?0, 'recipientId': ?1}")
    Optional<FriendRequest> findBySenderIdAndRecipientId(String senderId, String recipientId);
    
    @Query("{'senderId': ?0, 'recipientId': ?1, 'status': ?2}")
    Optional<FriendRequest> findBySenderIdAndRecipientIdAndStatus(
        String senderId, String recipientId, FriendRequest.Status status);
    
    @Query("{ $or: [ {'senderId': ?0}, {'recipientId': ?0} ] }")
    List<FriendRequest> findBySenderIdOrRecipientId(String userId);
    
    @Query("{'recipientId': ?0, 'status': 'PENDING'}")
    List<FriendRequest> findByRecipientIdAndStatus(String recipientId, FriendRequest.Status status);
    
    @Query("{'senderId': ?0, 'status': 'PENDING'}")
    List<FriendRequest> findBySenderIdAndStatus(String senderId, FriendRequest.Status status);
    
    @Query("{ $or: [ {'senderId': ?0, 'recipientId': ?1}, {'senderId': ?1, 'recipientId': ?0} ] }")
    List<FriendRequest> findBySenderIdAndRecipientIdOrRecipientIdAndSenderId(
        String userId1, String userId2, String friendId, String userId);
}