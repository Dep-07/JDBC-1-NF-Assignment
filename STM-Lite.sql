USE student;
ALTER TABLE phone_number ADD CONSTRAINT fk FOREIGN KEY (id) REFERENCES student_details(id);


