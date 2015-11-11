# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table user (
  id                        bigint auto_increment not null,
  retired                   tinyint(1) default 0,
  first_name                varchar(255),
  last_name                 varchar(255),
  username                  varchar(255),
  password                  varchar(255),
  email                     varchar(255),
  type                      integer,
  creator_id                bigint,
  created_time              datetime(6) not null,
  updated_time              datetime(6) not null,
  constraint ck_user_type check (type in (0,1,2,3)),
  constraint uq_user_username unique (username),
  constraint pk_user primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table user;

SET FOREIGN_KEY_CHECKS=1;

