package com.csye.webapp.controllers;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.csye.webapp.configuration.statsD;
import com.csye.webapp.model.User;
import com.csye.webapp.repository.UserRepository;

import com.google.gson.JsonObject;
import com.timgroup.statsd.StatsDClient;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.PublishRequest;

//controler
@RestController
public class UserController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	StatsDClient statsDClient;

	AmazonDynamoDB dynamodbClient;

	AmazonSNS snsClient;

	Long expirationTTL;

	@Value("${snstopicArn}")
	private String snstopic;
	HttpHeaders responseHeaders = new HttpHeaders();
	// private final static Logger LOG =
	// LoggerFactory.getLogger(UserController.class);

	final static Logger LOGGIN_LOGGER = LoggerFactory.getLogger(UserController.class);
	String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

	// ccreating a uuser via POST
	@PostMapping(value = "/v1/account")
	public ResponseEntity<?> userCreatEntity(@RequestBody(required = false) User uuser) {
		statsDClient.incrementCounter("create user api");
		long startt_t = System.currentTimeMillis();
		LOGGIN_LOGGER.info("Inisde Post create request");

		if (uuser == null) {
			JsonObject entity = new JsonObject();
			entity.addProperty("message", "Request Body cannot be null");
			long end_t = System.currentTimeMillis();
			LOGGIN_LOGGER.error("Please enter request body; it can not be null");
			return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);

		}
		if (userRepository.findByemail(uuser.getEmail()) != null) {
			JsonObject entity = new JsonObject();
			entity.addProperty("message", "user exists already");
			long end_t = System.currentTimeMillis();
			statsDClient.recordExecutionTime("postUserApiTime", (end_t - startt_t));
			LOGGIN_LOGGER.error("user already exists");
			return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
		}
		// String dateFormat = simpleDateFormat.format(new Date());

		String date_formaString = simpleDateFormat.format(new Date());
		if ((uuser.getFirstName() != null && uuser.getFirstName().trim().length() > 0)
				&& (uuser.getLastName() != null && uuser.getLastName().trim().length() > 0) && uuser.getEmail() != null
				&& uuser.getPassword() != null) {
			if (validatePassword(uuser.getPassword()) && validateEmail(uuser.getEmail())) {
				User sUser = new User();
				sUser.setFirstName(uuser.getFirstName());
				sUser.setLastName(uuser.getLastName());
				sUser.setPassword(BCrypt.hashpw(uuser.getPassword(), BCrypt.gensalt()));
				sUser.setEmail(uuser.getEmail());
				sUser.setAccount_created(date_formaString.toString());
				sUser.setAccount_updated(date_formaString.toString());
				long startuserdb = System.currentTimeMillis();
				userRepository.save(sUser);
				long end_tuserdb = System.currentTimeMillis();
				long timeDif = (end_tuserdb - startuserdb);
				statsDClient.recordExecutionTime("Postuserdb", timeDif);
				sUser.setPassword(null);
				// long end_t = System.currentTimeMillis();
				// statsDClient.recordExecutionTime("postUserApiTime", end_t-startt_t);
				// LOGGIN_LOGGER.info("User Created with time :" + (end_t - startt_t));
				// JsonObject entity = new JsonObject();
				long end = System.currentTimeMillis();
				// LOGGIN_LOGGER.info("User created wit time: " + (end - start));

				dynamodbClient = AmazonDynamoDBClientBuilder.defaultClient();
				LOGGIN_LOGGER.info("successfully built dynamodbClient");

				Instant expirationInstant = Instant.now().plusSeconds(300);
				LOGGIN_LOGGER.info("expirationInstant=" + expirationInstant);
				expirationTTL = expirationInstant.getEpochSecond();
				LOGGIN_LOGGER.info("expirationTTL=" + expirationTTL);
				String token = UUID.randomUUID().toString();

				PutItemRequest itemRequest = new PutItemRequest();
				itemRequest.setTableName("csye6225");
				itemRequest.setReturnValues(ReturnValue.ALL_OLD);
				itemRequest.setReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
				Map<String, AttributeValue> map = new HashMap<>();
				map.put("id", new AttributeValue(sUser.getEmail()));
				map.put("AccessToken", new AttributeValue(token));
				map.put("TTL", new AttributeValue(expirationTTL.toString()));
				map.put("emailSent", new AttributeValue(expirationTTL.toString()));
				itemRequest.setItem(map);
				try {
					dynamodbClient.putItem(itemRequest);
				} catch (Exception ex) {
					LOGGIN_LOGGER.info("Dynamo Exception:  " + ex.getStackTrace());
				}
				LOGGIN_LOGGER.info("Dynamodb put successful");

				snsClient = AmazonSNSClientBuilder.defaultClient();

				JSONObject json = new JSONObject();
				json.put("AccessToken", token);
				json.put("EmailAddress", sUser.getEmail());
				json.put("MessageType", "email");

				PublishRequest publishRequest = new PublishRequest()
						.withTopicArn(snstopic)
						.withMessage(json.toString());
				snsClient.publish(publishRequest);
				return new ResponseEntity<User>(sUser, HttpStatus.CREATED);
			}

			JsonObject entity = new JsonObject();
			entity.addProperty("Validation Error", "Please enter email and password as per standard convention");
			long end_t = System.currentTimeMillis();
			statsDClient.recordExecutionTime("postUserApiTime", (end_t - startt_t));
			LOGGIN_LOGGER.error("Please enter email and password as per standard convention");
			return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
		}

		JsonObject entity = new JsonObject();
		entity.addProperty("message",
				"Email, FirstName, LastName and Password all four fields cannot be null or First and Last Name cannot be blank");
		long end_t = System.currentTimeMillis();
		statsDClient.recordExecutionTime("postUserApiTime", (end_t - startt_t));
		LOGGIN_LOGGER.error(
				"Email, FirstName, LastName and Password all four fields cannot be null or First and Last Name cannot be blank");
		return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
	}

	@GetMapping(value = "/v1/account/{id}")
	public ResponseEntity<?> getUser(@PathVariable String id, HttpServletRequest request,
			HttpServletResponse response) {
		// statsDClient.incrementCounter("create user api");
		statsDClient.incrementCounter("uuser.get");
		LOGGIN_LOGGER.info("Inside Get User Api");
		long startt_t = System.currentTimeMillis();

		// System.out.println(sUser.toString());
		if (userRepository.findById(id).isEmpty()) {
			return new ResponseEntity<String>("Please enter valid Id", HttpStatus.UNAUTHORIZED);
		}

		HttpHeaders responseHeaders = new HttpHeaders();
		String autho = request.getHeader("Authorization");
		JsonObject entity = new JsonObject();
		if (autho != null && autho.toLowerCase().startsWith("basic")) {
			// Authorization: Basic base64credentials
			autho = autho.replaceFirst("Basic ", "");

			String CRED = new String(Base64.getDecoder().decode(autho.getBytes()));

			// autho = username:password
			String[] userCred = CRED.split(":", 2);
			String email = userCred[0];

			String password = userCred[1];
			if (!userRepository.findById(id).get().getEmail().equals(email)) {
				return new ResponseEntity<String>("Please enter valid email address corresponding to ID",
						HttpStatus.UNAUTHORIZED);
			}
			User uuser = userRepository.findByemail(email);
			if (uuser == null) {
				long end_t = System.currentTimeMillis();
				statsDClient.recordExecutionTime("getUserApiTime", (end_t - startt_t));
				LOGGIN_LOGGER.error("Please enter correct Username or Password");
				entity.addProperty("message", "Please enter correct Username or Password");
			} else if (uuser != null && !bCryptPasswordEncoder.matches(password, uuser.getPassword())) {
				long end_t = System.currentTimeMillis();
				statsDClient.recordExecutionTime("getUserApiTime", (end_t - startt_t));
				LOGGIN_LOGGER.error("The Password is Invalid");
				entity.addProperty("message", "The Password is Invalid");
			} else if (!(uuser.isIs_verified())) {

				LOGGIN_LOGGER.info("check if the user is verified");
				entity.addProperty("message", "User is not verified");

				return new ResponseEntity<String>(entity.toString(), HttpStatus.OK);
			} else {
				uuser.setPassword(null);
				// String jsonUser=new GsonBuilder().setPrettyPrinting().create().toJson(uuser);
				responseHeaders.set("MyResponseHeader", "MyValue");
				long end_t = System.currentTimeMillis();
				statsDClient.recordExecutionTime("getUserApiTime", (end_t - startt_t));
				LOGGIN_LOGGER.info("User successfully retrieved");
				return new ResponseEntity<User>(uuser, HttpStatus.OK);
			}

			return new ResponseEntity<String>(entity.toString(), HttpStatus.UNAUTHORIZED);

		}

		entity.addProperty("message", "Invalid. Unable to Authenticate");
		long end_t = System.currentTimeMillis();
		statsDClient.recordExecutionTime("getUserApiTime", (end_t - startt_t));
		LOGGIN_LOGGER.error("Invalid. Unable to Authenticate");
		return new ResponseEntity<String>(entity.toString(), HttpStatus.UNAUTHORIZED);
	}

	// Update User
	@PutMapping(value = "/v1/account/{id}")
	public ResponseEntity<String> putUser(@PathVariable String id, @RequestBody(required = false) User uuser,
			HttpServletRequest request, HttpServletResponse response) {
		// statsDClient.incrementCounter("create user api");
		LOGGIN_LOGGER.info("Inside Put User Api");
		statsDClient.incrementCounter("uuser.put");
		if (userRepository.findById(id).isEmpty()) {
			return new ResponseEntity<String>("Please enter valid Id", HttpStatus.UNAUTHORIZED);
		}
		LOGGIN_LOGGER.info("put API");
		long startt_t = System.currentTimeMillis();
		String autho = request.getHeader("Authorization");
		JsonObject entity = new JsonObject();
		String date_formaString = simpleDateFormat.format(new Date());
		if (autho != null && autho.toLowerCase().startsWith("basic")) {
			// Authorization: Basic base64credentials
			autho = autho.replaceFirst("Basic ", "");
			String CRED = new String(Base64.getDecoder().decode(autho.getBytes()));
			// autho = username:password
			String[] userCred = CRED.split(":", 2);
			String email = userCred[0];
			String password = userCred[1];
			if (!userRepository.findById(id).get().getEmail().equals(email)) {
				return new ResponseEntity<String>("Please enter valid email address corresponding to ID",
						HttpStatus.UNAUTHORIZED);
			}
			User sUser = userRepository.findByemail(email);
			if (sUser == null) {
				// long end_t = System.currentTimeMillis();
				// statsDClient.recordExecutionTime("putUserApiTime", (end_t-startt_t));
				LOGGIN_LOGGER.error("Please enter correct Username or Password");
				entity.addProperty("message", "Please enter correct Username or Password");
			} else if (sUser != null && !bCryptPasswordEncoder.matches(password, sUser.getPassword())) {
				// long end_t = System.currentTimeMillis();
				// statsDClient.recordExecutionTime("putUserApiTime", (end_t-startt_t));
				LOGGIN_LOGGER.error("Invalid Password");
				entity.addProperty("message", "Invalid Password");
			} else {
				if (uuser == null) {
					entity.addProperty("message", "Please provide request body");
					// long end_t = System.currentTimeMillis();
					// statsDClient.recordExecutionTime("putUserApiTime", (end_t-startt_t));
					LOGGIN_LOGGER.error("null request body, please provide request body");
					return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);

				} else if (!(uuser.isIs_verified())) {

					entity.addProperty("message", "User is not verified");

					return new ResponseEntity<String>(entity.toString(), HttpStatus.OK);
				} else if ((uuser.getFirstName() != null && uuser.getFirstName().trim().length() > 0)
						&& (uuser.getLastName() != null && uuser.getLastName().trim().length() > 0)
						&& uuser.getEmail() != null && uuser.getPassword() != null) {
					// updating here
					if (uuser.getEmail().equals(sUser.getEmail())) {

						if (validatePassword(uuser.getPassword()) && validateEmail(uuser.getEmail())) {
							String pw_hash = BCrypt.hashpw(uuser.getPassword(), BCrypt.gensalt());
							sUser.setFirstName(uuser.getFirstName());
							sUser.setLastName(uuser.getLastName());
							sUser.setPassword(pw_hash);
							sUser.setAccount_updated(date_formaString.toString());
							long startuserdb = System.currentTimeMillis();
							userRepository.save(sUser);
							long end_tuserdb = System.currentTimeMillis();
							statsDClient.recordExecutionTime("Putuserdb", (end_tuserdb - startuserdb));
							long end_t = System.currentTimeMillis();
							statsDClient.recordExecutionTime("putUserApiTime", (end_t - startt_t));
							LOGGIN_LOGGER.info("User successfully updated in time : " + (end_t - startt_t));
							return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
						} else {
							// JsonObject jsonObject = new JsonObject();
							entity.addProperty("Validation Error", "Please input correct values");
							long end_t = System.currentTimeMillis();
							statsDClient.recordExecutionTime("putUserApiTime", (end_t - startt_t));
							LOGGIN_LOGGER.error("Please eneter correct values");
							return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
						}

					} else if (userRepository.findByemail(uuser.getEmail()) == null) {
						// JsonObject jsonObject = new JsonObject();
						entity.addProperty("Message", "User cannot update email ");
						long end_t = System.currentTimeMillis();
						statsDClient.recordExecutionTime("putUserApiTime", (end_t - startt_t));
						LOGGIN_LOGGER.error("User cannot update email ");
						return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
					} else if (userRepository.findByemail(uuser.getEmail()) != null
							&& !uuser.getEmail().equals(sUser.getEmail())) {
						// JsonObject jsonObject = new JsonObject();
						entity.addProperty("Message", "uuser cannot update information of another uuser");
						long end_t = System.currentTimeMillis();
						statsDClient.recordExecutionTime("putUserApiTime", (end_t - startt_t));
						LOGGIN_LOGGER.error("uuser cannot update information of another uuser");
						return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
					} else {
						entity.addProperty("Message", "Email cannot be updated ");
						long end_t = System.currentTimeMillis();
						statsDClient.recordExecutionTime("putUserApiTime", (end_t - startt_t));
						LOGGIN_LOGGER.error("Email cannot be updated ");
						return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
					}
				} else {
					// JsonObject jsonObject = new JsonObject();
					entity.addProperty("message",
							"Email, FirstName, LastName and Password all four fields cannot be null or First and Last Name cannot be blank");
					long end_t = System.currentTimeMillis();
					statsDClient.recordExecutionTime("putUserApiTime", (end_t - startt_t));
					LOGGIN_LOGGER.error(
							"Email, FirstName, LastName and Password all four fields cannot be null or First and Last Name cannot be blank");
					return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
				}

			}
			return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);

		}

		entity.addProperty("message", "Invalid. Unable to Authenticate");
		long end_t = System.currentTimeMillis();
		statsDClient.recordExecutionTime("putUserApiTime", (end_t - startt_t));
		LOGGIN_LOGGER.error("Invalid. Unable to Authenticate");
		return new ResponseEntity<String>(entity.toString(), HttpStatus.UNAUTHORIZED);
	}

	public Boolean validatePassword(String password) {
		if (password != null || (!password.equalsIgnoreCase(""))) {
			String pattern = "^(?=.*?[A-Z])(?=(.*[a-z]){1,})(?=(.*[\\d]){1,})(?=(.*[\\W]){1,})(?!.*\\s).{9,16}$";
			return (password.matches(pattern));
		} else {
			return Boolean.FALSE;
		}

	}

	public Boolean validateEmail(String email) {
		if (email != null || (!email.equalsIgnoreCase(""))) {
			String emailvalidator = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
					+ "A-Z]{2,7}$";

			return email.matches(emailvalidator);
		} else {
			return Boolean.FALSE;
		}

	}

	@GetMapping("/v1/verifyUserEmail")
	public ResponseEntity<?> verifyUser(@RequestParam String email, @RequestParam String token) {

		JsonObject responseEntity = new JsonObject();

		LOGGIN_LOGGER.info("Inside Verify User Email");
		if (email == null || "".equals(email.trim())) {
			LOGGIN_LOGGER.error("Email address required");
			responseEntity.addProperty("message", "Email required");
			return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.BAD_REQUEST);
		}
		if (token == null || "".equals(token.trim())) {
			LOGGIN_LOGGER.error("Token required");
			responseEntity.addProperty("message", "Token required");
			return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.BAD_REQUEST);
		}
		email = email.trim();
		token = token.trim();
		dynamodbClient = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB dynamoDB = new DynamoDB(dynamodbClient);
		Table table = dynamoDB.getTable("csye6225");
		Item item = table.getItem("id", email);
		if (item == null || !item.get("AccessToken").equals(token)) {
			LOGGIN_LOGGER.error("Invalid token");
			responseEntity.addProperty("message", "Invalid Token");
			return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.BAD_REQUEST);
		}
		if (Long.parseLong(item.get("TTL").toString()) < Long
				.parseLong(String.valueOf(Instant.now().getEpochSecond()))) {
			LOGGIN_LOGGER.error("Token has expired");
			responseEntity.addProperty("message", "Token has expired");
			return new ResponseEntity<String>(responseEntity.toString(), HttpStatus.BAD_REQUEST);
		}
		User user = userRepository.findByemail(email.trim());
		userRepository.verifyUser(user);
		return new ResponseEntity<String>("User is verified", HttpStatus.OK);
	}

}
