CREATE TABLE users (
                       user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

CREATE TABLE algorithms (
                            algorithm_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL,
                            category VARCHAR(50),
                            description TEXT,
                            time_complexity VARCHAR(50),
                            space_complexity VARCHAR(50)
);

CREATE TABLE visualization_sessions (
                                        session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        user_id BIGINT NOT NULL,
                                        algorithm_id BIGINT NOT NULL,
                                        input_data TEXT,
                                        status VARCHAR(30),
                                        FOREIGN KEY (user_id) REFERENCES users(user_id),
                                        FOREIGN KEY (algorithm_id) REFERENCES algorithms(algorithm_id)
);

CREATE TABLE execution_steps (
                                 step_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 session_id BIGINT NOT NULL,
                                 step_number INT,
                                 step_data TEXT,
                                 action VARCHAR(100),
                                 FOREIGN KEY (session_id) REFERENCES visualization_sessions(session_id)
);

CREATE TABLE progress (
                          progress_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,
                          algorithm_id BIGINT NOT NULL,
                          status VARCHAR(30),
                          last_practiced DATETIME,
                          FOREIGN KEY (user_id) REFERENCES users(user_id),
                          FOREIGN KEY (algorithm_id) REFERENCES algorithms(algorithm_id)
);

CREATE TABLE visualization_history (
                                       history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       user_id BIGINT NOT NULL,
                                       session_id BIGINT NOT NULL,
                                       completed_at DATETIME,
                                       FOREIGN KEY (user_id) REFERENCES users(user_id),
                                       FOREIGN KEY (session_id) REFERENCES visualization_sessions(session_id)
);