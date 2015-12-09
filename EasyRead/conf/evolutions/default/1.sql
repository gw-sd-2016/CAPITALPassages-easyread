# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table pos (
  id                        bigint auto_increment not null,
  word_id                   bigint not null,
  name                      varchar(255),
  constraint pk_pos primary key (id))
;

create table passage_question (
  id                        bigint auto_increment not null,
  simple_passage_id         bigint not null,
  basis_id                  bigint,
  active                    tinyint(1) default 0,
  disavowed                 tinyint(1) default 0,
  position                  integer,
  correct_answer            varchar(255),
  created_time              datetime(6) not null,
  updated_time              datetime(6) not null,
  constraint pk_passage_question primary key (id))
;

create table passage_question_answer (
  id                        bigint auto_increment not null,
  text                      longtext,
  choice_id                 bigint,
  constraint pk_passage_question_answer primary key (id))
;

create table passage_question_choice (
  id                        bigint auto_increment not null,
  passage_question_id       bigint not null,
  entity_id                 bigint,
  correct                   tinyint(1) default 0,
  active                    tinyint(1) default 0,
  disavowed                 tinyint(1) default 0,
  position                  integer,
  created_time              datetime(6) not null,
  updated_time              datetime(6) not null,
  constraint pk_passage_question_choice primary key (id))
;

create table passage_question_prompt (
  id                        bigint auto_increment not null,
  text                      longtext,
  question_id               bigint,
  constraint pk_passage_question_prompt primary key (id))
;

create table passage_question_record (
  id                        bigint auto_increment not null,
  cleared                   tinyint(1) default 0,
  disavowed                 tinyint(1) default 0,
  user_id                   bigint,
  question_id               bigint,
  created_time              datetime(6) not null,
  updated_time              datetime(6) not null,
  constraint pk_passage_question_record primary key (id))
;

create table passage_question_response (
  id                        bigint auto_increment not null,
  passage_question_record_id bigint not null,
  entity_id                 bigint,
  disavowed                 tinyint(1) default 0,
  submitter_id              bigint,
  created_time              datetime(6) not null,
  updated_time              datetime(6) not null,
  constraint pk_passage_question_response primary key (id))
;

create table passage_tag (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  description               varchar(255),
  type                      varchar(255),
  disavowed                 tinyint(1) default 0,
  created_time              datetime(6) not null,
  updated_time              datetime(6) not null,
  constraint uq_passage_tag_name unique (name),
  constraint pk_passage_tag primary key (id))
;

create table sentence (
  id                        bigint auto_increment not null,
  simple_passage_id         bigint not null,
  text                      longtext,
  constraint pk_sentence primary key (id))
;

create table simple_passage (
  id                        bigint auto_increment not null,
  title                     varchar(255),
  text                      longtext,
  source                    varchar(255),
  grade                     integer,
  instructor_id             bigint,
  tag_id                    bigint,
  num_characters            integer,
  num_syllables             integer,
  num_words                 integer,
  constraint pk_simple_passage primary key (id))
;

create table suggestion (
  id                        bigint auto_increment not null,
  word                      varchar(255),
  suggested_word            varchar(255),
  simple_passage_id         bigint,
  constraint pk_suggestion primary key (id))
;

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

create table word (
  id                        bigint auto_increment not null,
  word_id                   bigint not null,
  word_net_id               bigint,
  lemma                     varchar(255),
  length                    integer,
  num_syllables             integer,
  age_of_acquisition        double,
  disavowed                 tinyint(1) default 0,
  created_time              datetime(6) not null,
  updated_time              datetime(6) not null,
  constraint pk_word primary key (id))
;


create table simple_passage_passage_tag (
  simple_passage_id              bigint not null,
  passage_tag_id                 bigint not null,
  constraint pk_simple_passage_passage_tag primary key (simple_passage_id, passage_tag_id))
;
alter table pos add constraint fk_pos_word_1 foreign key (word_id) references word (id) on delete restrict on update restrict;
create index ix_pos_word_1 on pos (word_id);
alter table passage_question add constraint fk_passage_question_simple_passage_2 foreign key (simple_passage_id) references simple_passage (id) on delete restrict on update restrict;
create index ix_passage_question_simple_passage_2 on passage_question (simple_passage_id);
alter table passage_question_choice add constraint fk_passage_question_choice_passage_question_3 foreign key (passage_question_id) references passage_question (id) on delete restrict on update restrict;
create index ix_passage_question_choice_passage_question_3 on passage_question_choice (passage_question_id);
alter table passage_question_record add constraint fk_passage_question_record_user_4 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_passage_question_record_user_4 on passage_question_record (user_id);
alter table passage_question_record add constraint fk_passage_question_record_question_5 foreign key (question_id) references passage_question (id) on delete restrict on update restrict;
create index ix_passage_question_record_question_5 on passage_question_record (question_id);
alter table passage_question_response add constraint fk_passage_question_response_passage_question_record_6 foreign key (passage_question_record_id) references passage_question_record (id) on delete restrict on update restrict;
create index ix_passage_question_response_passage_question_record_6 on passage_question_response (passage_question_record_id);
alter table passage_question_response add constraint fk_passage_question_response_submitter_7 foreign key (submitter_id) references user (id) on delete restrict on update restrict;
create index ix_passage_question_response_submitter_7 on passage_question_response (submitter_id);
alter table sentence add constraint fk_sentence_simple_passage_8 foreign key (simple_passage_id) references simple_passage (id) on delete restrict on update restrict;
create index ix_sentence_simple_passage_8 on sentence (simple_passage_id);
alter table word add constraint fk_word_word_9 foreign key (word_id) references word (id) on delete restrict on update restrict;
create index ix_word_word_9 on word (word_id);



alter table simple_passage_passage_tag add constraint fk_simple_passage_passage_tag_simple_passage_01 foreign key (simple_passage_id) references simple_passage (id) on delete restrict on update restrict;

alter table simple_passage_passage_tag add constraint fk_simple_passage_passage_tag_passage_tag_02 foreign key (passage_tag_id) references passage_tag (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table pos;

drop table passage_question;

drop table passage_question_answer;

drop table passage_question_choice;

drop table passage_question_prompt;

drop table passage_question_record;

drop table passage_question_response;

drop table passage_tag;

drop table sentence;

drop table simple_passage;

drop table simple_passage_passage_tag;

drop table suggestion;

drop table user;

drop table word;

SET FOREIGN_KEY_CHECKS=1;

