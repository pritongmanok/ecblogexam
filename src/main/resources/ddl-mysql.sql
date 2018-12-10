CREATE DATABASE IF NOT EXISTS workdayexam;

USE workdayexam;

DROP TABLE IF EXISTS `CONTRIBUTORS`;
DROP TABLE IF EXISTS `USER_SESSION`;
DROP TABLE IF EXISTS `BLOG_ENTRY`;
DROP TABLE IF EXISTS `BLOG_SPACE`;
DROP TABLE IF EXISTS `USER`;

CREATE TABLE USER (
  USER_NAME varchar(100) primary key,
  EMAIL varchar(100) not null,
  PASSWORD varchar(100) not null
);

CREATE TABLE USER_SESSION (
  SESSION_ID varchar(100) primary key,
  USER_NAME varchar(100),
  CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  EXPIRED BOOLEAN,
  foreign key fk_sess_user(USER_NAME)
  references USER(USER_NAME)
);

CREATE TABLE BLOG_SPACE (
  ID  bigint generated by default as identity (start with 1 increment by 1) primary key ,
  OWNER varchar(100),
  URI varchar(100) not null,
  foreign key fk_space_user(OWNER)
  references USER(USER_NAME)
);

CREATE TABLE CONTRIBUTORS (
  USER_NAME varchar(100),
  SPACE_ID bigint,
  foreign key fk_contrib_user(USER_NAME)
  references USER(USER_NAME),
  foreign key fk_contrib_space(SPACE_ID)
  references BLOG_SPACE(ID)
);

CREATE TABLE BLOG_ENTRY (
  ID  bigint generated by default as identity (start with 1 increment by 1) primary key ,
  AUTHOR varchar(100),
  SPACE_ID bigint,
  URI varchar(100) not null,
  APPROVED BOOLEAN,
  foreign key fk_entry_space(SPACE_ID)
  references BLOG_SPACE(ID)
);

-- From grepping all the select statements in the code, we're able
-- to choose a few candidates for optimizations.
-- In reality, we should observe actual load pattern and determine
-- which tables actually need indexing.

alter table `CONTRIBUTORS` add index `USER_SPACE_INDEX` (USER_NAME, SPACE_ID);
alter table `BLOG_SPACE` add index `OWNER_INDEX` (OWNER);










