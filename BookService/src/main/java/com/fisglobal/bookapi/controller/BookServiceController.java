package com.fisglobal.bookapi.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fisglobal.bookapi.entity.Books;
import com.fisglobal.bookapi.jwt.AuthenticationRequest;
import com.fisglobal.bookapi.jwt.AuthenticationResponse;
import com.fisglobal.bookapi.jwt.BookSecurityService;
import com.fisglobal.bookapi.jwt.JWTUtil;
import com.fisglobal.bookapi.repository.BookRepository;
import com.fisglobal.bookapi.service.BookService;

@RestController
public class BookServiceController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	BookRepository bookRepository;
	
	@Autowired
	BookService bookService;
	
	@Autowired
	BookSecurityService bookSecurityService;
	
	@Autowired
	JWTUtil jwtUtil;
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@GetMapping("/books")
	public List<Books> getAllBooks(){
		return bookRepository.findAll();
		
	}
	
	@GetMapping("/books/{bookId}")
	public Books getBookByBookId(@PathVariable("bookId") String bookId) {		
		return bookRepository.findByBookId(bookId);
	}

	@PostMapping("/books/updateavailability/{bookId}/{incrementalCount}")
	public Books updateBookAvailability(@PathVariable("bookId") String bookId,@PathVariable("incrementalCount") int incCount) {
		Books book = bookService.updateSubscribedBooks(bookId, incCount);
		return book;
	}
	
	@PostMapping("/authenticate")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authReq) throws Exception{
		
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authReq.getUserNm(), authReq.getPasswd()));
		}catch (BadCredentialsException e) {
			throw new Exception("Incorrect username or password", e);
		}
		
		final UserDetails usrDetails = bookSecurityService.loadUserByUsername(authReq.getUserNm());
		
		final String jwtToken = jwtUtil.generateToken(usrDetails);
		
		return ResponseEntity.ok(new AuthenticationResponse(jwtToken));
		
	}
}
