#################################################
# Copy table matches into a new table, leaving
# the original table as backup in matches_backup
################################################

DROP TABLE IF EXISTS matches_backup;
DROP TABLE IF EXISTS matches_temp;
select count(*) from matches;

CREATE TABLE matches_temp (
  user1                 INTEGER,
  user2                 INTEGER,
  last_match_event_time TIMESTAMP null DEFAULT null,
  CONSTRAINT PRIMARY KEY (user1, user2),
  INDEX matches_user1_idx (user1),
  INDEX matches_user2_idx (user2),
  CONSTRAINT FOREIGN KEY (user1) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT FOREIGN KEY (user2) REFERENCES users (id) ON DELETE CASCADE
)
ENGINE=InnoDB
DEFAULT CHARSET=latin1;

REPLACE 
INTO matches_temp (user1, user2, last_match_event_time)
SELECT * from matches;

RENAME TABLE matches TO matches_backup, matches_temp TO matches;

select count(*) from matches_backup;
select count(*) from matches;

#################################################
# Copy table opinions into a new table, leaving
# the original table as backup in opinion_backup
################################################

DROP TABLE IF EXISTS opinions_temp;
DROP TABLE IF EXISTS opinions_backup;

CREATE TABLE opinions_temp (
  target_user_id        INTEGER,
  originating_user_id   INTEGER,
  originator_experience INTEGER NOT null DEFAULT 0,
  type                  CHAR(1)  NOT null,
  CONSTRAINT PRIMARY KEY (target_user_id, originating_user_id),
  INDEX opinions_target_user_id_idx (target_user_id),
  INDEX opinions_originating_user_id_idx (originating_user_id),
  CONSTRAINT FOREIGN KEY (target_user_id)      REFERENCES users (id) ON DELETE CASCADE, 
  CONSTRAINT FOREIGN KEY (originating_user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT TYPE_V_OR_C CHECK (type='C' OR type='V'),
  CONSTRAINT POSITIVE_EXPERIENCE CHECK (originator_experience >= 0)
)
ENGINE=InnoDB
DEFAULT CHARSET=latin1;

INSERT into opinions_temp
(target_user_id, originating_user_id, originator_experience, type)
SELECT * from opinions;

RENAME TABLE opinions TO opinions_backup, opinions_temp TO opinions;

select count(*) from opinions_backup;
select count(*) from opinions;

