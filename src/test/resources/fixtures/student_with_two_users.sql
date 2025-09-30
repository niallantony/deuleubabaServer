INSERT INTO app_user (user_id, email, name, user_type, username, role_id) VALUES
    ('user', 'email@email.com', 'name', 'teacher', 'user1', 1),
    ('user2', 'email@email.com', 'name', 'teacher', 'user2', 1);
INSERT INTO student (student_id, age, disability, grade, name, school, setting) VALUES
    ('abc', 12, 'Disability', 3, 'John', 'School', 'General');
INSERT INTO user_students (user_id, student_id) VALUES
    ('user', 'abc'),
    ('user2', 'abc');