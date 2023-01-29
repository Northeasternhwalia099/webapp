# WebApp

## Technology Stack
Programming Language and Framework Used: Java, Spring Boot Framework, MySQL

## Prerequisites
Make sure you have the following tools installed:
- [VS Code](https://code.visualstudio.com/)
- [Postman](https://www.postman.com/downloads/)
- [MySQL](https://www.mysql.com/)

## Build Instructions
1. Import the application from `webapp/project` folder into VS Code.
2. Configure the `application.properties` by adding your database connection.
3. Run the application.

### Testing the API
1. Open Postman and select the POST option.
2. Enter the URL as "http://localhost:8080/v1/account" with key `Content-Type`.
3. In the body section, select 'raw' and then choose 'JSON(application/json)'.
4. Write the parameters in JSON format and click 'Send'.
5. View the results in the window below.
6. If the username already exists or password length does not match, the required status code/message is shown.

### Accessing Account Information
1. Select GET option and enter the URL as "http://localhost:8080/v1/account/{id}".
2. In the 'authorization' section, select 'Basic Auth'.
3. Enter the credentials provided in step 7 and click 'Send'.
4. If the credentials are correct, the given response is shown with correct status codes.

### Updating User Information
1. Select PUT option and enter the URL as "http://localhost:8080/v1/account/{id}".
2. If the credentials are correct, the user is updated with correct status codes.

## Document Uploads in S3 Buckets
### Endpoints
- Uploading document for a particular user: "http://localhost:8080/v1/documents/"
- Getting a particular user document: "http://localhost:8080/v1/document/{id}"
- Get all documents for a particular user: "http://localhost:8080/v1/documents/"
- Delete documents for a particular user: "http://localhost:8080/v1/documents/"
