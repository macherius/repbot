alter table users 
add column
  news_level int not null default 0;

create index users_news_level on users ( news_level );

create table news (
  level int auto_increment primary key,
  message varchar(255) not null
) type=InnoDB;