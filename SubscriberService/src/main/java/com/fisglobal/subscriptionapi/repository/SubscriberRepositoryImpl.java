package com.fisglobal.subscriptionapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.fisglobal.subscriptionapi.entity.Subscribers;
import com.mongodb.client.result.UpdateResult;

@Repository
public class SubscriberRepositoryImpl implements SubscriberRepositoryCustom{
	
	 @Autowired
	 MongoTemplate mongoTemplate;

	@Override
	public long updateSubscription(Subscribers subscribers) {
		Query query = new Query();
		query.addCriteria(Criteria.where("bookId").is(subscribers.getBookId()));
		query.addCriteria(Criteria.where("subscriberId").is(subscribers.getSubscriberId()));
		query.addCriteria(Criteria.where("dateReturned").is(""));
		Update update = new Update();
        update.set("dateReturned", subscribers.getDateReturned()); 

        UpdateResult result = mongoTemplate.updateFirst(query, update, Subscribers.class);

        if(result!=null)
            return result.getModifiedCount();
        else
            return 0;
	}

}
