-- Postgres 8.4 or better SQL schema for RepBot 2.2.x

/*
 * CLEANUP (resets to empty DB)
 */

DROP IF EXISTS TABLE news;
DROP IF EXISTS TABLE matches;
DROP IF EXISTS TABLE opinions;
DROP IF EXISTS TABLE users;
DROP IF EXISTS TABLE aliases_created_by_users;
DROP IF EXISTS TABLE aliases;

DROP IF EXISTS INDEX matches_timestamp_idx;
DROP IF EXISTS INDEX alias_a_idx;
DROP IF EXISTS INDEX alias_b_idx;
DROP IF EXISTS INDEX optinions_originators_idx;
DROP IF EXISTS INDEX users_name_idx;
DROP IF EXISTS INDEX users_last_verified_idx;
DROP IF EXISTS INDEX users_last_logout_idx;

DROP IF EXISTS SEQUENCE news_level_seq;
DROP IF EXISTS SEQUENCE users_id_seq;

DROP IF EXISTS FUNCTION upsert_match_event(text, text, timestamp with time zone);

/*
 * USERS
 */

CREATE TABLE users (
  id 			SERIAL,
  name 			VARCHAR(255) 		NOT null UNIQUE,
  news_level 	INTEGER 			NOT null DEFAULT 0, -- FIXME: REFERENCES news (level) --
  alert 		BOOLEAN 			NOT null DEFAULT false,
  bot 			BOOLEAN 			NOT null DEFAULT false,
  -- aliased		BOOLEAN				NOT null DEFAULT false,
  last_verified TIMESTAMP 			WITH TIME ZONE,
  last_logout	TIMESTAMP 			WITH TIME ZONE,
  last_experience INTEGER			DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE index users_name_idx 			ON users ( name );
CREATE index users_last_verified_idx	ON users ( last_verified );
CREATE index users_last_logout_idx		ON users ( last_logout );

/*
 * OPINIONS
 */


CREATE TABLE opinions (
  target_user_id		INTEGER			REFERENCES users (id) ON DELETE CASCADE,
  originating_user_id	INTEGER 		REFERENCES users (id) ON DELETE CASCADE,
  originator_experience INTEGER			NOT null,
  type char(1)							NOT null,
  PRIMARY KEY (target_user_id, originating_user_id)
);

CREATE index optinions_originators_idx on opinions ( originating_user_id );

/*
 *  NEWS
 */

CREATE TABLE news (
  level 	SERIAL,
  message	VARCHAR(255)	NOT null,
  PRIMARY KEY (level)
);

/*
 * MATCHES
 */

CREATE TABLE matches (
	user_lower_AZ_order		VARCHAR(255),
	user_higher_AZ_order	VARCHAR(255),
	last_match_event_time	TIMESTAMP 		WITH TIME ZONE DEFAULT '1992-07-19 05:51:00-00',
	PRIMARY KEY  (user_lower_AZ_order, user_higher_AZ_order)
);

CREATE index matches_timestamp_idx on matches ( last_match_event_time );

/* See: http://www.postgresql.org/docs/9.2/static/plpgsql-control-structures.html */
CREATE OR REPLACE FUNCTION upsert_match_event(user1 TEXT, user2 TEXT) RETURNS VOID AS
$upsert_match_event$
DECLARE
	uloweraz TEXT;
	uhigheraz TEXT;
BEGIN
	-- Relation is maintained with lexicographically ordered names to save a lot of ORs in the
	-- actual queries.
	IF user1 < user2 THEN
		uloweraz := user1;
		uhigheraz := user2;
	ELSE
		uloweraz := user2;
		uhigheraz := user1;
	END IF;
	
    LOOP
        -- first try to update the key
    	UPDATE matches SET last_match_event_time = now() 
    	WHERE user_lower_AZ_order = uloweraz AND user_higher_AZ_order = uhigheraz ;
        IF found THEN
            RETURN;
        END IF;
        -- not there, so try to insert the key
        -- if someone else inserts the same key concurrently,
        -- we could get a unique-key failure
        BEGIN
            INSERT INTO matches(user_lower_AZ_order, user_higher_AZ_order, last_match_event_time)
            VALUES (uloweraz, uhigheraz, now());
            RETURN;
        EXCEPTION WHEN unique_violation THEN
            -- Do nothing, and loop to try the UPDATE again.
        END;
    END LOOP;
END;
$upsert_match_event$
LANGUAGE plpgsql;

/*
 * ALIASES
 */

CREATE TABLE aliases_created_by_users (
	a					INTEGER				REFERENCES users (id) ON DELETE CASCADE,
	b					INTEGER				REFERENCES users (id) ON DELETE CASCADE,
	created_time		TIMESTAMP 			WITH TIME ZONE,
	created_by			VARCHAR(255),				
	PRIMARY KEY (a, b)
);

CREATE TABLE aliases (
	a					INTEGER				REFERENCES users (id) ON DELETE CASCADE,
	b					INTEGER				REFERENCES users (id) ON DELETE CASCADE,
	PRIMARY KEY (a, b)
);
CREATE index alias_a_idx ON aliases ( a );
CREATE index alias_b_idx ON aliases ( b );

CREATE OR REPLACE FUNCTION insert_alias_safe(aa INTEGER, bb INTEGER) RETURNS VOID AS
$insert_alias_safe$
BEGIN
	INSERT INTO aliases VALUES (aa, bb);
	EXCEPTION WHEN unique_violation THEN
		-- Do nothing
END;
$insert_alias_safe$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION on_insert_aliases() RETURNS trigger AS
$on_insert_aliases$
    BEGIN
        PERFORM insert_alias_safe(NEW.a, NEW.b);
        PERFORM insert_alias_safe(NEW.b, NEW.a);
/*
WITH RECURSIVE transitive_closure(a, b, path) AS
( SELECT  a, b, ARRAY[a, b] AS path
  FROM aliases_created_by_users AS a, users AS u
  WHERE a.a = u.id AND u.name='inim'
 
  UNION
 
  SELECT tc.a, a.b, path || a.b AS path
  FROM aliases_created_by_users AS a
  JOIN transitive_closure AS tc ON a.a = tc.b
  WHERE a.b <> ALL (tc.path)
),
aliased_ids AS 
(
SELECT tc.a AS id FROM transitive_closure AS tc
UNION
SELECT tc.b AS id FROM transitive_closure AS tc
)

INSERT INTO aliases
SELECT l.id AS a, r.id AS b
FROM aliased_ids AS l, aliased_ids AS r
WHERE l.id <> r.id
;

 */        
        RETURN NEW;
	END;
$on_insert_aliases$
LANGUAGE plpgsql;

CREATE TRIGGER on_insert_aliases
AFTER INSERT ON aliases_created_by_users
FOR EACH ROW EXECUTE PROCEDURE on_insert_aliases();

CREATE OR REPLACE FUNCTION on_delete_aliases() RETURNS trigger AS
$on_delete_aliases$
    BEGIN
	    DELETE FROM aliases WHERE (a = NEW.a AND b = NEW.b) OR (a = NEW.b AND b = NEW.a);
        RETURN NEW;
	END;
$on_delete_aliases$
LANGUAGE plpgsql;

CREATE TRIGGER on_delete_aliases
AFTER DELETE ON aliases_created_by_users
FOR EACH ROW EXECUTE PROCEDURE on_delete_aliases();

