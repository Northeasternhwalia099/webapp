#!/bin/sh

      sudo apt -y update
      sudo apt -y install default-jre
      sudo apt install -y openjdk-18-jdk
      
      sudo apt -y install mysql-server
      sudo systemctl start mysql.service
      sudo mysql -u root -pXoxo@9898 -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Xoxo@9898';"
      sudo mysql -u root  -pXoxo@9898 -e  "create database users;"
    
      sudo mv /tmp/appservice.service /etc/systemd/system/appservice.service
      sudo systemctl enable appservice.service
      sudo systemctl start appservice.service
      sudo systemctl status appservice.service