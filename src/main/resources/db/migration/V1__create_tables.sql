-- Создание таблиц
CREATE TABLE countries (
                           name VARCHAR(40),
                           country_id CHAR(3) UNIQUE,
                           area_sqkm INT,
                           population INT
);

CREATE TABLE olympics (
                          olympic_id CHAR(7) UNIQUE,
                          country_id CHAR(3) REFERENCES countries(country_id),
                          city VARCHAR(50),
                          year INT,
                          startdate DATE,
                          enddate DATE
);

CREATE TABLE players (
                         name VARCHAR(40),
                         player_id CHAR(10) UNIQUE,
                         country_id CHAR(3) REFERENCES countries(country_id),
                         birthdate DATE
);

CREATE TABLE events (
                        event_id CHAR(7) UNIQUE,
                        name VARCHAR(40),
                        eventtype CHAR(20),
                        olympic_id CHAR(7) REFERENCES olympics(olympic_id),
                        is_team_event BOOLEAN,
                        num_players_in_team INT,
                        result_noted_in VARCHAR(100)
);

CREATE TABLE results (
                         event_id CHAR(7) REFERENCES events(event_id),
                         player_id CHAR(10) REFERENCES players(player_id),
                         medal CHAR(7),
                         result DOUBLE PRECISION
);
