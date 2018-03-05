DROP TABLE IF EXISTS matches; 
CREATE TABLE matches (
	user_lower_AZ_order varchar(255) NOT NULL,
	user_higher_AZ_order varchar(255) NOT NULL,
	last_match_event_time datetime default '1992-07-19 05:51:00',
	PRIMARY KEY  (user_lower_AZ_order, user_higher_AZ_order)
);

CREATE INDEX matches_timestamp on matches ( last_match_event_time );