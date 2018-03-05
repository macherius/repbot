-- MariaDB 10.1 or better SQL schema for RepBot 2.2.x

/*
 * CLEANUP (resets to empty DB)
 */

DROP TABLE IF EXISTS news;
DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS opinions;
DROP TABLE IF EXISTS users;

/*
 *  NEWS
 */

CREATE TABLE news (
  level     INTEGER      NOT null,
  message   VARCHAR(255) NOT null,
  CONSTRAINT PRIMARY KEY (level)
)
ENGINE=InnoDB
DEFAULT CHARSET=latin1;

/*
 * USERS
 */

CREATE TABLE users (
  id              INTEGER      NOT null AUTO_INCREMENT,
  name            VARCHAR(255) NOT null,
  news_level      INTEGER      NOT null DEFAULT 0,
  alert           BOOLEAN      NOT null DEFAULT false,
  bot             BOOLEAN      NOT null DEFAULT false,
  last_verified   TIMESTAMP        null DEFAULT null,
  last_logout     TIMESTAMP,
  last_experience INTEGER      NOT null DEFAULT 0,
  CONSTRAINT PRIMARY KEY (id),
  CONSTRAINT UNIQUE INDEX (name),
  CONSTRAINT POSITIVE_EXPERIENCE CHECK (last_experience >= 0)
)
ENGINE=InnoDB 
DEFAULT CHARSET=latin1;

/*
 * OPINIONS
 */

CREATE TABLE opinions (
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

/*
 * MATCHES
 */

CREATE TABLE matches (
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
