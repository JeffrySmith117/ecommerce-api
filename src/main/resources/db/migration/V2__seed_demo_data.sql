-- Dados de demonstração: permitem que o ambiente publicado (deploy) já tenha
-- categorias, produtos e um usuário ADMIN para gerenciar o catálogo, sem
-- exigir nenhum passo manual após o deploy.
--
-- Os produtos referenciam categorias por nome (não por ID) e os inserts usam
-- ON CONFLICT DO NOTHING, para que a migration seja segura mesmo que rode
-- sobre um banco que já tenha alguns desses registros (ex: ambiente local
-- usado durante o desenvolvimento).

INSERT INTO categories (name) VALUES
    ('Eletrônicos'),
    ('Informática'),
    ('Livros'),
    ('Casa e Decoração')
ON CONFLICT (name) DO NOTHING;

INSERT INTO products (name, description, price, stock_qty, category_id) VALUES
    ('Fone de Ouvido Bluetooth', 'Fone sem fio com cancelamento de ruído e 30h de bateria', 249.90, 50,
        (SELECT id FROM categories WHERE name = 'Eletrônicos')),
    ('Smartwatch Fit 2', 'Relógio inteligente com monitor cardíaco e GPS', 399.00, 30,
        (SELECT id FROM categories WHERE name = 'Eletrônicos')),
    ('Caixa de Som Portátil', 'Caixa de som à prova d''água com 12h de autonomia', 189.90, 40,
        (SELECT id FROM categories WHERE name = 'Eletrônicos')),
    ('Notebook Dell Inspiron', 'Notebook com processador Intel Core i7, 16GB RAM e SSD 512GB', 3499.99, 10,
        (SELECT id FROM categories WHERE name = 'Informática')),
    ('Mouse Sem Fio', 'Mouse ergonômico com sensor óptico de precisão', 79.90, 100,
        (SELECT id FROM categories WHERE name = 'Informática')),
    ('Teclado Mecânico', 'Teclado mecânico RGB com switches azuis', 259.90, 25,
        (SELECT id FROM categories WHERE name = 'Informática')),
    ('Monitor 27" Full HD', 'Monitor com taxa de atualização de 75Hz', 899.00, 15,
        (SELECT id FROM categories WHERE name = 'Informática')),
    ('Clean Code', 'Livro sobre boas práticas de desenvolvimento de software, de Robert C. Martin', 89.90, 60,
        (SELECT id FROM categories WHERE name = 'Livros')),
    ('O Programador Pragmático', 'Guia clássico sobre desenvolvimento de software', 74.90, 45,
        (SELECT id FROM categories WHERE name = 'Livros')),
    ('Luminária de Mesa LED', 'Luminária com ajuste de intensidade e temperatura de cor', 119.90, 35,
        (SELECT id FROM categories WHERE name = 'Casa e Decoração')),
    ('Jogo de Panelas Antiaderente', 'Conjunto com 5 peças antiaderentes', 349.90, 20,
        (SELECT id FROM categories WHERE name = 'Casa e Decoração')),
    ('Organizador de Gaveta', 'Kit com 6 divisórias ajustáveis', 59.90, 70,
        (SELECT id FROM categories WHERE name = 'Casa e Decoração'));

-- Usuário administrador de demonstração.
-- Email: admin@ecommerce.com | Senha: Admin123!
-- (hash BCrypt gerado previamente; a senha em texto puro não fica no código)
INSERT INTO users (name, email, password, role) VALUES
    ('Admin Demo', 'admin@ecommerce.com', '$2a$10$xAOLVd2FRtrxRmwifxsizuLe.6y5/8thPKKCG2e.rVKk9nce/KU/C', 'ADMIN')
ON CONFLICT (email) DO NOTHING;
