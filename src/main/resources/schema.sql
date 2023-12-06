DROP TABLE IF EXISTS Post;

CREATE TABLE Post (
  id varchar(255) NOT NULL,
  title varchar(255) NOT NULL,
  slug varchar(255) NOT NULL,
  date date NOT NULL,
  time_to_read int NOT NULL,
  tags varchar(255),
  version INT,
  PRIMARY KEY (id)
);

INSERT INTO Post
(id, title, slug, date, time_to_read, tags, version)
VALUES (1,'Hello, World!','hello-world',CURRENT_DATE, 5, 'Spring Boot, Java', null);