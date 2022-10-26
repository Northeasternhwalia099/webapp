# webapp
Technology Stack
Programming Language and framework used: Java, Spring Boot Framework, MySQL

Prerequisites for building the application:
VS code\
Install Postman\
Install MySQL\
Build Instructions\
Import the application from webapp/project folder into VS code\
Configure the application.properties by adding your database connection\
Run the application\
To test the API results, go to Postman application\
Now select the POST option and enter the URL as "http://localhost:8080/v1/account" with key Content-Type\
In the body section below, select 'raw' and then select 'JSON(application/json)'\
Write the parameters to be sent in JSON format and click on 'Send', see the results on the window below\
If the username already exists or password length does not match, required status code/message is shown\
Now select GET option and enter the URL as "http://localhost:8080/v1/account/{id}"\
In the 'authorization' section, select 'Basic Auth'\
Enter the credentials provided in step 7 and click 'Send'\
If the credentials are correct, the given response is shown with correct status codes\
Now select PUT option and enter the URL as "http://localhost:8080/v1/account/{id}"\
If the credentials are correct, the user is updated correct status codes\
#checking update readme #readme ass04
run test by maven: mvn test
