DELETE FROM professors_subjects;
DELETE FROM professors;
DELETE FROM subjects;
DELETE FROM students;
DELETE FROM users;
VACUUM;

INSERT INTO users (username, password, role) VALUES
('profesor.ana', '$2a$10$tEKNhC2dG4hFGokS2LZP5exFAnCb0woeY1Rpl4L/wbOezRNqoKx5m', 1),
('profesor.luis','$2a$10$tEKNhC2dG4hFGokS2LZP5exFAnCb0woeY1Rpl4L/wbOezRNqoKx5m', 1),
('profesor.sofia','$2a$10$tEKNhC2dG4hFGokS2LZP5exFAnCb0woeY1Rpl4L/wbOezRNqoKx5m', 1);


INSERT INTO professors (name, last_name, email, id_employee, user_id) VALUES
('Ana', 'Lopez', 'ana.lopez@universidad.com', 'P001', 1),
('Luis', 'Garcia', 'luis.garcia@universidad.com', 'P002', 2),
('Sofía', 'Martinez', 'sofia.martinez@universidad.com', 'P003', 3);

INSERT INTO subjects (name) VALUES
('Algoritmos y Estructuras de Datos'),
('Sistemas Operativos'),
('Redes de Computadoras'),
('Ingeniería de Software'),
('Cálculo Avanzado');

-- INSERT INTO professors_subjects (professor_id, subject_id) VALUES
--(1, 1),
--(1, 2),
--(1, 4);

--INSERT INTO professors_subjects (professor_id, subject_id) VALUES
--(2, 2),
--(2, 3),
--(2, 5);

--INSERT INTO professors_subjects (professor_id, subject_id) VALUES
--(3, 1),
--(3, 4),
--(3, 5);
