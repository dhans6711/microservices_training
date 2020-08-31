package com.fisglobal.subscriptionapi.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fisglobal.subscriptionapi.entity.Subscribers;
import com.fisglobal.subscriptionapi.jwt.AuthenticationRequest;
import com.fisglobal.subscriptionapi.jwt.AuthenticationResponse;
import com.fisglobal.subscriptionapi.jwt.JWTUtil;
import com.fisglobal.subscriptionapi.jwt.SubscriberSecurityService;
import com.fisglobal.subscriptionapi.repository.SubscriberRepository;
import com.fisglobal.subscriptionapi.service.SubscriberService;

@RestController
public class SubscriberServiceController {
	
	@Autowired
	SubscriberRepository subscriberRepository;
	
	@Autowired
	SubscriberService subscriberService;
	
	@Autowired
	private KafkaTemplate<String,String> kafkaTemp;
	
	@Autowired
	SubscriberSecurityService subscriberSecurityService;
	
	@Autowired
	JWTUtil jwtUtil;
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@GetMapping("/subscriptions")
	public List<Subscribers> getAllSubscriptions(@RequestParam Optional<String> subscriberId){
		
		if(subscriberId.isPresent()) {
			List<Subscribers> subscriber= subscriberRepository.findAllBySubscriberId(subscriberId.get());
			return subscriber;
		}
		
		return subscriberRepository.findAll();
		
	}
	
	@PostMapping("/subscriptions")
	public String addNewSubscription(@RequestBody Subscribers subscribers) {
		String msg=null;
		if(subscribers != null) {
			msg = subscriberService.addNewSubscription(subscribers);
		}else {
			msg = "Subscription failed";
		}
		return msg;
	}
	
	@PostMapping("/returns")
	public String returnBooks(@RequestBody Subscribers subscribers) {
		return subscriberService.returnSubscribedBooks(subscribers);
	}
	
	/*
	 * @GetMapping("/kafka/subscriber/{id}") public String
	 * sendProducerMsg(@PathVariable("id") String id) {
	 * kafkaTemp.send("ProductTopic",id); return "Product Msg Sent: " + id; }
	 */
	
	@PutMapping("/kafka/subscriber/{subscriberId}/{bookId}")
	public String sendProducerMsgWithKey(@PathVariable("subscriberId") String subId,@PathVariable("bookId") String bookId) {
		kafkaTemp.send("SubscriberTopic",subId,bookId);
		return "Message Subscribed for : " + subId + " and Book : "+ bookId;
	}
	
	@PostMapping("/authenticate")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authReq) throws Exception{
		
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authReq.getUserNm(), authReq.getPasswd()));
		}catch (BadCredentialsException e) {
			throw new Exception("Incorrect username or password", e);
		}
		
		final UserDetails usrDetails = subscriberSecurityService.loadUserByUsername(authReq.getUserNm());
		
		final String jwtToken = jwtUtil.generateToken(usrDetails);
		
		return ResponseEntity.ok(new AuthenticationResponse(jwtToken));
		
	}
}
