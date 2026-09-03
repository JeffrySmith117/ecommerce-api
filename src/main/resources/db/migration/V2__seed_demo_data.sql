-- Dados de demonstração: permitem que o ambiente publicado (deploy) já tenha
-- categorias, produtos e um usuário ADMIN para gerenciar o catálogo, sem
-- exigir nenhum passo manual após o deploy.
--
-- Os inserts são idempotentes (seguros mesmo rodando duas vezes sobre o
-- mesmo banco, ex: ambiente local usado durante o desenvolvimento).

INSERT INTO categories (name) VALUES
    ('Eletrônicos'),
    ('Informática'),
    ('Livros'),
    ('Casa e Decoração')
ON CONFLICT (name) DO NOTHING;

-- products.name não tem constraint UNIQUE (é só um catálogo de demonstração,
-- não uma regra de negócio), então a idempotência aqui é garantida por
-- NOT EXISTS em vez de ON CONFLICT.
INSERT INTO products (name, description, price, stock_qty, category_id)
SELECT v.name, v.description, v.price, v.stock_qty, c.id
FROM (VALUES
    ('Fone de Ouvido Bluetooth', 'Fone sem fio com cancelamento de ruído e 30h de bateria', 249.90, 50, 'Eletrônicos'),
    ('Smartwatch Fit 2', 'Relógio inteligente com monitor cardíaco e GPS', 399.00, 30, 'Eletrônicos'),
    ('Caixa de Som Portátil', 'Caixa de som à prova d''água com 12h de autonomia', 189.90, 40, 'Eletrônicos'),
    ('Notebook Dell Inspiron', 'Notebook com processador Intel Core i7, 16GB RAM e SSD 512GB', 3499.99, 10, 'Informática'),
    ('Mouse Sem Fio', 'Mouse ergonômico com sensor óptico de precisão', 79.90, 100, 'Informática'),
    ('Teclado Mecânico', 'Teclado mecânico RGB com switches azuis', 259.90, 25, 'Informática'),
    ('Monitor 27" Full HD', 'Monitor com taxa de atualização de 75Hz', 899.00, 15, 'Informática'),
    ('Clean Code', 'Livro sobre boas práticas de desenvolvimento de software, de Robert C. Martin', 89.90, 60, 'Livros'),
    ('O Programador Pragmático', 'Guia clássico sobre desenvolvimento de software', 74.90, 45, 'Livros'),
    ('Luminária de Mesa LED', 'Luminária com ajuste de intensidade e temperatura de cor', 119.90, 35, 'Casa e Decoração'),
    ('Jogo de Panelas Antiaderente', 'Conjunto com 5 peças antiaderentes', 349.90, 20, 'Casa e Decoração'),
    ('Organizador de Gaveta', 'Kit com 6 divisórias ajustáveis', 59.90, 70, 'Casa e Decoração')
) AS v(name, description, price, stock_qty, category_name)
JOIN categories c ON c.name = v.category_name
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.name = v.name);

-- Usuário administrador de demonstração, pensado para uso em ambiente LOCAL.
-- Email: admin@ecommerce.com | Senha: Admin123!
--
-- Atenção: se este projeto for publicado em um ambiente acessível
-- publicamente, troque a senha deste usuário diretamente no banco de dados
-- de produção logo após o deploy (esta migration não deve ser a fonte da
-- senha final de um admin em produção, já que o arquivo fica público no
-- repositório). ON CONFLICT DO NOTHING garante que, se o e-mail já existir
-- (com outra senha ou outro papel), o registro existente não é sobrescrito.
INSERT INTO users (name, email, password, role) VALUES
    ('Admin Demo', 'admin@ecommerce.com', '$2a$10$xAOLVd2FRtrxRmwifxsizuLe.6y5/8thPKKCG2e.rVKk9nce/KU/C', 'ADMIN')
ON CONFLICT (email) DO NOTHING;
