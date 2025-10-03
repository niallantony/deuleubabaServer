INSERT INTO app_user (user_id, email, name, user_type, username, role_id) VALUES
    ('user', 'email@email.com', 'name', 'teacher', 'user1', 1);
INSERT INTO student (student_id, age, disability, grade, name, school, setting) VALUES
                                                                                    ('abc', 12, 'Disability', 3, 'John', 'School', 'General'),
                                                                                    ('def', 10, 'Disability', 2, 'Ernie', 'School', 'General');
INSERT INTO user_students (user_id, student_id) VALUES
                                                    ('user', 'abc'),
                                                    ('user', 'def');
