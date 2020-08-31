package com.fisglobal.subscriptionapi.repository;

import org.springframework.stereotype.Service;

import com.fisglobal.subscriptionapi.entity.Subscribers;

@Service
public interface SubscriberRepositoryCustom {
	
	long updateSubscription(Subscribers subscribers);

}
