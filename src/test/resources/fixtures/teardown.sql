DELETE FROM user_students;
DELETE FROM entry_communication_category;
DELETE FROM dictionary_entry;
DELETE FROM student;
DELETE FROM app_user;
ALTER SEQUENCE dictionary_entry_id_seq RESTART;