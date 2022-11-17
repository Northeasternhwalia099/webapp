#!/bin/sh

      # sudo apt -y update
      # # sudo apt install ruby -y
      # # sudo apt install wget -y
      # sudo apt install -y amazon-cloudwatch-agent 
      # #sudo apt -y install default-jre
      # sudo apt install -y openjdk-18-jdk
      
      # sudo mv /home/ubuntu/cloudwatch-config.json /etc/systemd/system/cloudwatch_config.json
      # # sudo apt -y install mysql-server
      # # sudo systemctl start mysql.service
      # # sudo mysql -u root -pXoxo@9898 -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Xoxo@9898';"
      # # sudo mysql -u root  -pXoxo@9898 -e  "create database users;"
    
      # sudo mv /tmp/appservice.service /etc/systemd/system/appservice.service
      # sudo systemctl enable appservice.service
      # sudo systemctl start appservice.service
      # sudo systemctl status appservice.service
      # # sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
      # #       -a fetch-config \
      # #       -m ec2 \
      # #       -c file:/etc/aws/cloudwatch_config.json \
            
      #!/bin/sh

      sudo apt -y update
      sudo apt install -y openjdk-11-jdk
      sudo mv /tmp/appservice.service /etc/systemd/system/appservice.service
      
      sudo systemctl daemon-reload
      sudo systemctl enable appservice.service
      sudo systemctl start appservice.service
      sudo systemctl status appservice.service
     
      sudo curl -o /root/amazon-cloudwatch-agent.deb https://s3.amazonaws.com/amazoncloudwatch-agent/debian/amd64/latest/amazon-cloudwatch-agent.deb
      sudo dpkg -i -E /root/amazon-cloudwatch-agent.deb
      sudo cp /home/ubuntu/cloudwatch-config.json /opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json
     # chmod 764 /opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json
      sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
          -a fetch-config \
          -m ec2 \
          -c file:/opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json \
          -s
      sudo service amazon-cloudwatch-agent start     
