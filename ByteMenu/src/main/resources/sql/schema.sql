-- ByteMenu Cafeteria Management System
-- Database Schema v1.0

CREATE TABLE IF NOT EXISTS users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name   TEXT    NOT NULL,
    email       TEXT    NOT NULL UNIQUE,
    password    TEXT    NOT NULL,
    role        TEXT    NOT NULL DEFAULT 'student',
    created_at  TEXT    DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS menu_items (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    category    TEXT    NOT NULL,
    price       REAL    NOT NULL,
    description TEXT,
    available   INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS orders (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    total       REAL    NOT NULL DEFAULT 0,
    status      TEXT    NOT NULL DEFAULT 'Preparing',
    notes       TEXT,
    created_at  TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id        INTEGER NOT NULL,
    menu_item_id    INTEGER NOT NULL,
    quantity        INTEGER NOT NULL DEFAULT 1,
    customization   TEXT,
    unit_price      REAL    NOT NULL,
    FOREIGN KEY (order_id)      REFERENCES orders(id),
    FOREIGN KEY (menu_item_id)  REFERENCES menu_items(id)
);

CREATE TABLE IF NOT EXISTS seat_reservations (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    seat_number TEXT    NOT NULL,
    date        TEXT    NOT NULL,
    time_slot   TEXT    NOT NULL,
    status      TEXT    NOT NULL DEFAULT 'Confirmed',
    created_at  TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Seed menu items
INSERT OR IGNORE INTO menu_items (name, category, price, description) VALUES
    ('Chicken Burger',   'Main Course', 350, 'Crispy fried chicken with lettuce and mayo'),
    ('Beef Burger',      'Main Course', 420, 'Juicy beef patty with cheese and pickles'),
    ('Veggie Wrap',      'Main Course', 280, 'Grilled vegetables in a tortilla wrap'),
    ('Chicken Biryani',  'Main Course', 300, 'Spiced basmati rice with tender chicken'),
    ('Pasta Alfredo',    'Main Course', 320, 'Creamy white sauce pasta with garlic bread'),
    ('French Fries',     'Snacks',      150, 'Crispy golden fries with ketchup'),
    ('Chicken Nuggets',  'Snacks',      200, '6-piece crispy chicken nuggets'),
    ('Spring Rolls',     'Snacks',      180, '4-piece vegetable spring rolls'),
    ('Pepsi',            'Drinks',      80,  '330ml chilled can'),
    ('7UP',              'Drinks',      80,  '330ml chilled can'),
    ('Mineral Water',    'Drinks',      50,  '500ml bottle'),
    ('Mango Juice',      'Drinks',      120, 'Fresh mango juice 250ml'),
    ('Chocolate Cake',   'Desserts',    200, 'Slice of rich chocolate fudge cake'),
    ('Gulab Jamun',      'Desserts',    120, '3-piece warm gulab jamun'),
    ('Ice Cream',        'Desserts',    150, 'Two scoops of vanilla ice cream');
