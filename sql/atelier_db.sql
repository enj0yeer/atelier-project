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

-- Демонстрационные услуги. Связанные с учётными записями клиенты, мастера
-- и заказ создаются DataInitializer при первом запуске приложения.
INSERT INTO services (name, description, price, active) VALUES
('Подгонка изделия по фигуре', 'Корректировка посадки готовой одежды', 1200.00, TRUE),
('Ремонт одежды', 'Замена молнии, ремонт швов и мелкие исправления', 900.00, TRUE),
('Пошив юбки', 'Индивидуальный пошив юбки по меркам клиента', 4500.00, TRUE),
('Пошив платья', 'Разработка и пошив платья по согласованной модели', 8500.00, TRUE),
('Укорочение брюк', 'Укорочение низа брюк с обработкой края', 700.00, TRUE)
ON CONFLICT (name) DO NOTHING;
