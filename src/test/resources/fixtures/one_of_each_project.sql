INSERT INTO project (id, completed, completed_on, description, imgsrc, objective, type, student_id, started_on, created_by)
VALUES (1 ,false, null , 'description', 'example.jpg', 'objective', 'COMMUNICATION', 'abc', '2000-01-01', 'user');
INSERT INTO project_communication_category (project_id, category_id) VALUES (1,6);
INSERT INTO project_user (is_completed, project_id, user_id)
VALUES (false, 1, 'user');

INSERT INTO project (id,completed, completed_on, description, imgsrc, objective, type, student_id, started_on, created_by)
VALUES (2, true, '2010-12-30', 'another description', 'example.jpg', 'objective', 'COMMUNICATION', 'abc', '2010-01-01', 'user');
INSERT INTO project_communication_category (project_id, category_id) VALUES (2,3);
INSERT INTO project_user (is_completed, completed_on, project_id, user_id)
VALUES (true, '2010-12-30', 2, 'user');

INSERT INTO project (id, completed, completed_on, description, imgsrc, objective, type, student_id, started_on, created_by)
VALUES (3 , false, null , 'description', 'example.jpg', 'objective', 'COMMUNICATION', 'abc', CURRENT_DATE + INTERVAL '1 day', 'user');
INSERT INTO project_communication_category (project_id, category_id) VALUES (3,6);
INSERT INTO project_user (is_completed, project_id, user_id)
VALUES (false, 3, 'user');
