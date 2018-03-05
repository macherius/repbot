CREATE DATABASE repbot;
CREATE USER 'repbot'@'localhost' IDENTIFIED BY 'my-strong-password-here';

# https://mariadb.com/kb/en/library/grant/
#
GRANT ALL ON repbot.* to 'repbot'@'localhost';

# DROP USER IF EXISTS repbot;