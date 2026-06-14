CREATE DATABASE IF NOT EXISTS gym_crm_local;
USE gym_crm_local;

CREATE TABLE training_types (
    id                               BIGINT                AUTO_INCREMENT      PRIMARY KEY,
    training_type_name   VARCHAR(100)    NOT NULL                   UNIQUE
);

CREATE TABLE users (
    id                BIGINT                  AUTO_INCREMENT  PRIMARY KEY,
    first_name  VARCHAR(255)     NOT NULL,
    last_name   VARCHAR(255)     NOT NULL,
    username    VARCHAR(255)    NOT NULL               UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    is_active      BOOLEAN            NOT NULL                 DEFAULT TRUE
);

CREATE TABLE trainees (
    user_id            BIGINT                PRIMARY KEY,
    date_of_birth   DATE,
    address           VARCHAR(255),
    CONSTRAINT fk_trainee_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE trainers (
    user_id                 BIGINT      PRIMARY KEY,
    specialization_id   BIGINT      NOT NULL,
    CONSTRAINT fk_trainer_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_trainer_specialization FOREIGN KEY (specialization_id) REFERENCES training_types(id)
);

CREATE TABLE trainings (
    id                         BIGINT                  AUTO_INCREMENT      PRIMARY KEY,
    trainee_id             BIGINT                  NOT NULL,
    trainer_id              BIGINT                  NOT NULL,
    training_name       VARCHAR(255)    NOT NULL,
    training_type_id    BIGINT                  NOT NULL,
    training_date        DATE                    NOT NULL,
    duration                INT                       NOT NULL,
    CONSTRAINT fk_training_trainee FOREIGN KEY (trainee_id) REFERENCES trainees(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_training_trainer FOREIGN KEY (trainer_id) REFERENCES trainers(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_training_type FOREIGN KEY (training_type_id) REFERENCES training_types(id)
);

INSERT INTO training_types (training_type_name) VALUES 
('FITNESS'), ('CARDIO'), ('STRENGTH'), ('YOGA'), ('CROSSFIT');