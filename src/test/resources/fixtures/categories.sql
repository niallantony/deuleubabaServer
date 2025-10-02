TRUNCATE communication_category RESTART IDENTITY CASCADE;
INSERT INTO communication_category (label) VALUES
                                               ('ATTENTION'), ('HELP'), ('REQUEST'), ('SHOWME'), ('PAIN'), ('REJECTION');