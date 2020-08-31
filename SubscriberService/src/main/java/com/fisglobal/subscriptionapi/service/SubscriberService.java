package com.fisglobal.subscriptionapi.service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fisglobal.subscriptionapi.entity.Books;
import com.fisglobal.subscriptionapi.entity.Subscribers;
import com.fisglobal.subscriptionapi.repository.SubscriberRepository;

@Service
public class SubscriberService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	SubscriberRepository subscriberRepository;
	
	@Autowired
	RestTemplate restTemplate;
	
	@Value("${book.service.token}")
	String bookToken;
	
	@Value("${subscriber.service.token}")
	String subscriberToken;
	
	public String addNewSubscription(Subscribers subscribers) {
		
		String msg = null;
		
		String notifyFlg = subscribers.getNotifyFlag();
		String bookId = subscribers.getBookId();
		String subId = subscribers.getSubscriberId();
		
		Books book = isBookAvailable(bookId);
		
		if(book.getAvailableCopies()>0) {
			
			boolean isDuplicateSubscr = checkforDuplicateSubscription(subId,bookId);
		
			if(!isDuplicateSubscr) {
				Books updatedBook = updateSubscribedBooks(bookId, -1);
				
				if(updatedBook!=null) {
					logger.info("Book subscribed in Subscriber Service ---"+ updatedBook.getBookName());
					logger.info("Remaining Copies available in Subscriber Service"+updatedBook.getAvailableCopies());
					subscriberRepository.save(subscribers);
					msg = "Subscription Added";
				}			
			}else {
				msg = "Already Subscribed";
			}
		}else {
			if(notifyFlg.equalsIgnoreCase("Yes")) {
				logger.info("Drop a Kafka Message");
				boolean isNotifyOn = subscribeForNotification(bookId, subId);
				if(isNotifyOn) {
					msg = "Book Not Available & Book Notification Serice Added";
				}else {
					msg = "Book Not Available & Book Notification Serice Failed";
				}
			}else {
				msg = "Book Not Available";
			}
		}	
		
		return msg;
		
	}
	
	public String returnSubscribedBooks(Subscribers subscribers) {

		String bookId = subscribers.getBookId();
		Books book = updateSubscribedBooks(bookId, 1);
		String msg = null;
		
		if(book !=null) {
			logger.info("Book Returns in Subscriber Service");
		
			String kafkaMsg = readMessage(subscribers.getSubscriberId(),bookId);
			
			logger.info("Kafka Message Received : "+kafkaMsg);
			
			if(kafkaMsg.compareTo(bookId)==1) {
				logger.info("Notification initiated for SubscriberId : "+ subscribers.getSubscriberId() + 
						" for BookId :" +bookId);
			}
			 
			long val = subscriberRepository.updateSubscription(subscribers);
			logger.info("Returns in Subscriber Service updated : "+val);
			if(val==1) {
				msg = "Book Returned and Subscription updated";
			}else {
				msg = "Book Not Returned Successfully";
			}
		}else {
			msg = "Problem updating book subscription details";
		}
		
		return msg;
	}
	
	public boolean checkforDuplicateSubscription(String subscribeId,String bookId) {
		
		logger.info("Checking for duplicate subscription");
		
		Subscribers subscriber = new Subscribers();
		subscriber.setSubscriberId(subscribeId);
		subscriber.setBookId(bookId);
		subscriber.setDateReturned("");
		
		Example<Subscribers> example = Example.of(subscriber);
		boolean exists = subscriberRepository.exists(example);
		
		return exists;
	}
	
	public Books isBookAvailable(String bookId) {
		HttpHeaders hdrs = new HttpHeaders();
		hdrs.add("Authorization", "Bearer "+bookToken);
		HttpEntity<String> entity = new HttpEntity<String>(hdrs);
		
		String uri =  "http://BookService/books/{bookId}" ;
		
		ResponseEntity<Books> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<Books>() {},bookId);
		Books book=resp.getBody();
		
		return book;
	}
	
	public Books updateSubscribedBooks(String bookId, int incrementCount) {
		
		HttpHeaders hdrs = new HttpHeaders();
		hdrs.add("Authorization", "Bearer "+bookToken);
		HttpEntity<String> entity = new HttpEntity<String>(hdrs);
			
		String uri =  "http://BookService/books/updateavailability/"+bookId+"/"+incrementCount ;
		
		ResponseEntity<Books> resp = restTemplate.exchange(uri, HttpMethod.POST, entity, new ParameterizedTypeReference<Books>() {},bookId,incrementCount);
		
		if(resp!=null) {
			Books book=resp.getBody();
			return book;
		}else {
			return null;
		}
	}
	
	public boolean subscribeForNotification(String bookId,String subscriberId) {
		HttpHeaders hdrs = new HttpHeaders();
		hdrs.add("Authorization", "Bearer "+subscriberToken);
		HttpEntity<String> entity = new HttpEntity<String>(hdrs);
		
		String uri =  "http://SubscriberService/kafka/subscriber/"+subscriberId+"/"+bookId;
		
		ResponseEntity<String> resp = restTemplate.exchange(uri, HttpMethod.PUT, entity, new ParameterizedTypeReference<String>() {},subscriberId,bookId);
		
		if(resp!=null) {
			String msg = resp.getBody();
			logger.info("Message Dropped in Kafka for Subscriber and Book ");
			logger.info(msg);
			return true;
		}else {
			logger.info("Problem for Message Subscription");
			return false;
		}
	}
	
	private String readMessage(String subscriberId, String bookId) {
		
		logger.info("Lookup for existing Notification Topic");
		
		Properties prop = new Properties();
		
		prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
		prop.put(ConsumerConfig.GROUP_ID_CONFIG,"groupId1");
		prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"true");
		prop.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"1000");
		prop.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,"30000");
		prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
		prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);
		consumer.subscribe(Arrays.asList("SubscriberTopic"));
		
		StringBuilder str = new StringBuilder();
		
		int counter = 1;
		int giveup = 100;
		
		while(true) {
			ConsumerRecords<String, String> messages = consumer.poll(Duration.ofMillis(1000));
			logger.info("Counter : "+ counter++);
			
			if(messages.count()==0) {
				counter++;
	                if (counter > giveup) { 
	                	logger.info("No Message Subscription Available");
	                 	break;
	                }
	                else continue;
			}
		
			for(ConsumerRecord<String, String> msg : messages) {
			System.out.printf("Offset = %d, Value = %s\n ",msg.offset(),msg.value());
			System.out.printf("Offset = %d, Value = %s\n ",msg.offset(),msg.key());
				
				String msgSubId = msg.key();
				String msgbookId = msg.value();
				
				logger.info("Sub Message Key : " + msgSubId);
				logger.info("Sub Message Value : " + msgbookId);
				
				if(msgbookId.compareTo(bookId)==1) {
					str.append(msg.value());
					logger.info("Notification initiated for SubscriberId : "+ msgSubId + 
							" for BookId :" +msgbookId);
					break;
				}else {
					str.append("No Message Subscription Available");
				}
			}
		}
		consumer.close();		
		return str.toString();
	}
	
	@Bean
	@LoadBalanced
	private RestTemplate restTemplate() {
		
		  HttpComponentsClientHttpRequestFactory httpRqstFactory = new HttpComponentsClientHttpRequestFactory();
			/*
			 * httpRqstFactory.setConnectionRequestTimeout(30000);
			 * httpRqstFactory.setConnectTimeout(30000);
			 * httpRqstFactory.setReadTimeout(30000);
			 */
		  return new RestTemplate(httpRqstFactory);

	}
}
