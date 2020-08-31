package com.fisglobal.bookapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fisglobal.bookapi.entity.Books;
import com.fisglobal.bookapi.repository.BookRepository;

@Service
public class BookService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	BookRepository bookRepository;
	
	@Autowired
	RestTemplate restTemplate;
	
	@Value("${book.service.token}")
	String bookToken;
	
	public Books updateSubscribedBooks(String bookId, int incrementCount) {
		
		logger.info(" Checking for BookDetails in Book Service "+bookId);
		
		Books bookDet = getBookDetailsById(bookId);
		
		if(bookDet != null) {
			int availCopies = bookDet.getAvailableCopies();
		
			logger.info("Book name in Book Service----"+bookDet.getBookName());
			logger.info("No of Copies available in Book Service----"+bookDet.getAvailableCopies());

			bookDet.setAvailableCopies(availCopies+incrementCount);
			bookDet = bookRepository.save(bookDet);
			return bookDet;
		}else {
			return null;
		}	
	}
	
	private Books getBookDetailsById(String bookId) {
		HttpHeaders hdrs = new HttpHeaders();
		hdrs.add("Authorization", "Bearer "+bookToken);
		HttpEntity<String> entity = new HttpEntity<String>(hdrs);
		
		String uri =  "http://BookService/books/{bookId}" ;
		
		ResponseEntity<Books> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<Books>() {},bookId);
		return resp.getBody();
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
