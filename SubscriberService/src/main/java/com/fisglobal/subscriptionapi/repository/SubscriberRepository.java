package com.fisglobal.subscriptionapi.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fisglobal.subscriptionapi.entity.Subscribers;

@Repository
public interface SubscriberRepository extends MongoRepository<Subscribers, String>,SubscriberRepositoryCustom{
	
	List<Subscribers> findAllBySubscriberId(String subscriberId);

}
