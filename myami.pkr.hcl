variable "aws_region" {
  type    = string
  default = "us-east-1"
}



variable "source_ami" {
  type    = string
  default = "ami-08c40ec9ead489470" # Ubuntu 22.04 LTS
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "subnet_id" {
  type    = string
  default = "subnet-86dd78b7" #added my subnet
}


# https://www.packer.io/plugins/builders/amazon/ebs
source "amazon-ebs" "my-ami" {
  region          = "${var.aws_region}"
  ami_name        = "csye6225_${formatdate("YYYY_MM_DD_hh_mm_ss", timestamp())}"
  ami_description = "AMI for CSYE 6225"
  ami_regions = [
    "us-east-1",
  ]
  ami_users = ["278111652716"] #accounts where the ami will be available once created

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }


  instance_type = "t2.micro"
  source_ami    = "${var.source_ami}"
  ssh_username  = "${var.ssh_username}"
  subnet_id     = "${var.subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = 8
    volume_type           = "gp2"
  }
}

build {
  sources = ["source.amazon-ebs.my-ami"]


  provisioner "file" {

    source      = "./target/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/home/ubuntu/webapp-0.0.1-SNAPSHOT.jar"
  }

  provisioner "file" {
    source      = "./appservice.service"
    destination = "/tmp/appservice.service"

  }
  provisioner "file" {
    source      = "./cloudwatch-config.json"
    destination = "/home/ubuntu/cloudwatch-config.json"
  }

  provisioner "shell" {

    scripts = [
      "./mysql.sh"
    ]
  }
   post-processor "manifest" {

    output = "manifest.json"

    strip_path = true

    custom_data = {

    my_custom_data = "example"

}

}

}
