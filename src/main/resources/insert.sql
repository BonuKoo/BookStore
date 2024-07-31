-- Insert Role
INSERT INTO role (role_id, role_name, role_desc, is_expression) VALUES (1, 'ROLE_ADMIN', 'Administrator role', 'false');
INSERT INTO role (role_id, role_name, role_desc, is_expression) VALUES (2, 'ROLE_MANAGER', 'Manager role', 'false');
INSERT INTO role (role_id, role_name, role_desc, is_expression) VALUES (3, 'ROLE_USER', 'User role', 'false');
INSERT INTO role (role_id, role_name, role_desc, is_expression) VALUES (4, 'ROLE_DBA', 'Database Administrator role', 'false');

-- Insert Resources
INSERT INTO resources (resource_id, resource_name, http_method, order_num, resource_type) VALUES (1, '/admin/**', 'GET', 1, 'URL');
INSERT INTO resources (resource_id, resource_name, http_method, order_num, resource_type) VALUES (2, '/manager/**', 'GET', 2, 'URL');
INSERT INTO resources (resource_id, resource_name, http_method, order_num, resource_type) VALUES (3, '/user/**', 'GET', 3, 'URL');

-- Insert Role-Resources Mapping
INSERT INTO role_resources (role_id, resource_id) VALUES (1, 1);  -- ROLE_ADMIN has access to /admin/**
INSERT INTO role_resources (role_id, resource_id) VALUES (2, 2);  -- ROLE_MANAGER has access to /manager/**
INSERT INTO role_resources (role_id, resource_id) VALUES (3, 3);  -- ROLE_USER has access to /user/**
INSERT INTO role_resources (role_id, resource_id) VALUES (4, 1);  -- ROLE_DBA has access to /admin/**

-- Insert role_hierarchy
insert into role_hierarchy (id, role_name, parent_id) values (1,'ROLE_ADMIN',null);
insert into role_hierarchy (id, role_name, parent_id) values (2,'ROLE_MANAGER','1');
insert into role_hierarchy (id, role_name, parent_id) values (3,'ROLE_DBA','1');
insert into role_hierarchy (id, role_name, parent_id) values (4,'ROLE_USER','2');
insert into role_hierarchy (id, role_name, parent_id) values (5,'ROLE_USER','3');

-- 3202번 계정을 관리자로 바꾸었다는 가정 하에 진행

UPDATE ACCOUNT_ROLES SET ROLE_ID = '3' WHERE USERNAME = 'username123';
-- update account - 필요 X
--UPDATE ACCOUNT SET ROLES = 'ROLE_ADMIN' WHERE ID = 3202;

