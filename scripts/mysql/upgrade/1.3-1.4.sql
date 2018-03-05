alter table users 
add column
  last_verified timestamp;

create index users_last_verified on users ( last_verified );

update users
   set last_verified = now();

