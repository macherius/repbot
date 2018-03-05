
alter table users
  modify column last_verified datetime,
  add column last_logout datetime,
  add column last_experience int default 0;

create index users_last_logout on users ( last_logout );
