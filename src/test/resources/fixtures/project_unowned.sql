INSERT INTO app_user (user_id, email, name, user_type, username, role_id) VALUES
    ('user2', 'email2@email.com', 'name', 'teacher', 'user2', 1);
INSERT INTO project (id, completed, completed_on, description, imgsrc, objective, type, student_id, started_on, created_by)
VALUES (1,false, null, 'description', 'example.jpg', 'objective', 'COMMUNICATION', 'abc', '2000-01-01', 'user2');
INSERT INTO project_communication_category (project_id, category_id) VALUES (1,6);
INSERT INTO project_user (is_completed, project_id, user_id)
VALUES (false, 1, 'user');
INSERT INTO project_user (is_completed, project_id, user_id)
VALUES (false, 1, 'user2')
