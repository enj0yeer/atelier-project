-- Создание базы выполняется отдельной командой от имени пользователя PostgreSQL:
-- CREATE DATABASE atelier_db OWNER atelier;
-- Затем подключитесь к atelier_db и выполните этот файл.

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CLIENT', 'MASTER', 'ADMIN')),
    full_name VARCHAR(160) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(160),
    user_id BIGINT UNIQUE REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    position VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    user_id BIGINT UNIQUE REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(1000),
    price NUMERIC(12,2) NOT NULL CHECK (price > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    employee_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('NEW', 'IN_PROGRESS', 'READY', 'ISSUED')),
    total_price NUMERIC(12,2) NOT NULL CHECK (total_price >= 0),
    note VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price > 0),
    line_total NUMERIC(12,2) NOT NULL CHECK (line_total >= 0)
);

CREATE INDEX IF NOT EXISTS idx_orders_client_id ON orders(client_id);
CREATE INDEX IF NOT EXISTS idx_orders_employee_id ON orders(employee_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- Демонстрационные данные. Учётные записи с BCrypt-хешами создаются DataInitializer.
INSERT INTO services (name, description, price, active) VALUES
('Подгонка изделия по фигуре', 'Корректировка посадки готовой одежды', 1200.00, TRUE),
('Ремонт одежды', 'Замена молнии, ремонт швов и мелкие исправления', 900.00, TRUE),
('Пошив юбки', 'Индивидуальный пошив юбки по меркам клиента', 4500.00, TRUE),
('Пошив платья', 'Разработка и пошив платья по согласованной модели', 8500.00, TRUE),
('Укорочение брюк', 'Укорочение низа брюк с обработкой края', 700.00, TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO clients (full_name, phone, email)
SELECT v.full_name, v.phone, v.email
FROM (VALUES
    ('Смирнова Елена Павловна', '+7 900 111-11-11', 'smirnova@example.local'),
    ('Кузнецова Ольга Игоревна', '+7 900 222-22-22', 'kuznetsova@example.local'),
    ('Соколова Мария Андреевна', '+7 900 333-33-33', 'sokolova@example.local'),
    ('Попова Дарья Сергеевна', '+7 900 444-44-44', 'popova@example.local'),
    ('Морозова Ирина Викторовна', '+7 900 555-55-55', 'morozova@example.local')
) AS v(full_name, phone, email)
WHERE NOT EXISTS (SELECT 1 FROM clients c WHERE c.email = v.email);

INSERT INTO employees (full_name, position, active)
SELECT v.full_name, v.position, TRUE
FROM (VALUES
    ('Иванова Мария Сергеевна', 'Мастер-портной'),
    ('Павлова Наталья Олеговна', 'Закройщик'),
    ('Фёдорова Анна Ильинична', 'Мастер по ремонту одежды')
) AS v(full_name, position)
WHERE NOT EXISTS (SELECT 1 FROM employees e WHERE e.full_name = v.full_name AND e.position = v.position);

-- Заказы привязаны к демонстрационным клиентам, услугам и сотрудникам.
INSERT INTO orders (client_id, employee_id, order_date, due_date, status, total_price, note)
SELECT c.id, e.id, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_DATE + 3, 'IN_PROGRESS', 2400.00, 'Подгонка двух изделий'
FROM clients c CROSS JOIN employees e WHERE c.email='smirnova@example.local' AND e.full_name='Иванова Мария Сергеевна'
AND NOT EXISTS (SELECT 1 FROM orders WHERE note='Подгонка двух изделий');
INSERT INTO orders (client_id, employee_id, order_date, due_date, status, total_price, note)
SELECT c.id, e.id, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_DATE + 5, 'NEW', 4500.00, 'Пошив юбки'
FROM clients c CROSS JOIN employees e WHERE c.email='kuznetsova@example.local' AND e.full_name='Павлова Наталья Олеговна'
AND NOT EXISTS (SELECT 1 FROM orders WHERE note='Пошив юбки');
INSERT INTO orders (client_id, employee_id, order_date, due_date, status, total_price, note)
SELECT c.id, e.id, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_DATE + 1, 'READY', 8500.00, 'Платье для мероприятия'
FROM clients c CROSS JOIN employees e WHERE c.email='sokolova@example.local' AND e.full_name='Иванова Мария Сергеевна'
AND NOT EXISTS (SELECT 1 FROM orders WHERE note='Платье для мероприятия');
INSERT INTO orders (client_id, employee_id, order_date, due_date, status, total_price, note)
SELECT c.id, e.id, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_DATE + 4, 'NEW', 700.00, 'Укоротить брюки'
FROM clients c CROSS JOIN employees e WHERE c.email='popova@example.local' AND e.full_name='Фёдорова Анна Ильинична'
AND NOT EXISTS (SELECT 1 FROM orders WHERE note='Укоротить брюки');
INSERT INTO orders (client_id, employee_id, order_date, due_date, status, total_price, note)
SELECT c.id, e.id, CURRENT_TIMESTAMP - INTERVAL '14 days', CURRENT_DATE - 2, 'ISSUED', 900.00, 'Замена молнии'
FROM clients c CROSS JOIN employees e WHERE c.email='morozova@example.local' AND e.full_name='Фёдорова Анна Ильинична'
AND NOT EXISTS (SELECT 1 FROM orders WHERE note='Замена молнии');

INSERT INTO order_items (order_id, service_id, quantity, unit_price, line_total)
SELECT o.id, s.id, 2, s.price, 2400.00 FROM orders o JOIN services s ON s.name='Подгонка изделия по фигуре'
WHERE o.note='Подгонка двух изделий' AND NOT EXISTS (SELECT 1 FROM order_items i WHERE i.order_id=o.id);
INSERT INTO order_items (order_id, service_id, quantity, unit_price, line_total)
SELECT o.id, s.id, 1, s.price, s.price FROM orders o JOIN services s ON s.name='Пошив юбки'
WHERE o.note='Пошив юбки' AND NOT EXISTS (SELECT 1 FROM order_items i WHERE i.order_id=o.id);
INSERT INTO order_items (order_id, service_id, quantity, unit_price, line_total)
SELECT o.id, s.id, 1, s.price, s.price FROM orders o JOIN services s ON s.name='Пошив платья'
WHERE o.note='Платье для мероприятия' AND NOT EXISTS (SELECT 1 FROM order_items i WHERE i.order_id=o.id);
INSERT INTO order_items (order_id, service_id, quantity, unit_price, line_total)
SELECT o.id, s.id, 1, s.price, s.price FROM orders o JOIN services s ON s.name='Укорочение брюк'
WHERE o.note='Укоротить брюки' AND NOT EXISTS (SELECT 1 FROM order_items i WHERE i.order_id=o.id);
INSERT INTO order_items (order_id, service_id, quantity, unit_price, line_total)
SELECT o.id, s.id, 1, s.price, s.price FROM orders o JOIN services s ON s.name='Ремонт одежды'
WHERE o.note='Замена молнии' AND NOT EXISTS (SELECT 1 FROM order_items i WHERE i.order_id=o.id);

-- Внимание: демонстрационные clients, employees и orders создаются без связи с users.
-- Связь появляется только у пользователей, созданных через DataInitializer (client@atelier.local,
-- master@atelier.local) и через регистрацию. Это ожидаемое поведение демо-данных.
