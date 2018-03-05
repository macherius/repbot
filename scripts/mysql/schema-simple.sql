# RepBot schema

create table users (
  id int auto_increment primary key,
  name varchar(255) not null unique,
  news_level int not null default 0,
  alert int not null default 0,
  bot int not null default 0,
  last_verified datetime,
  last_logout datetime,
  last_experience int default 0
);

create index users_news_level on users ( news_level );
create index users_last_verified on users ( last_verified );
create index users_last_logout on users ( last_logout );

create table opinions (
  target_user_id int not null references users,
  originating_user_id int not null references users,
  originator_experience int not null,
  type char(1) not null,
  primary key (target_user_id, originating_user_id)
);

create index originators_idx on opinions ( originating_user_id );

create table news (
  level int auto_increment primary key,
  message varchar(255) not null
);

CREATE TABLE matches (
        user_lower_AZ_order varchar(255) NOT NULL,
        user_higher_AZ_order varchar(255) NOT NULL,
        last_match_event_time datetime default '1992-07-19 05:51:00',
        PRIMARY KEY  (user_lower_AZ_order, user_higher_AZ_order)
);

CREATE INDEX matches_timestamp on matches ( last_match_event_time );
