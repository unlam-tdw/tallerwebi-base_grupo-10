CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED WITH mysql_native_password BY 'user';
GRANT ALL PRIVILEGES ON valhalla.* TO 'user'@'%';
DROP USER IF EXISTS 'user'@'localhost';
FLUSH PRIVILEGES;
