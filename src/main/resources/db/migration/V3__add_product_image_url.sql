-- Adiciona um campo de imagem para os produtos (usado pela listagem no front-end).
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Preenche uma imagem de demonstração pra cada produto já existente (catálogo seed).
-- As imagens vêm do LoremFlickr, um serviço gratuito de imagens de placeholder por
-- palavra-chave, usado aqui só para fins de demonstração visual do catálogo.
UPDATE products SET image_url = 'https://loremflickr.com/400/300/book?lock=1' WHERE name = 'Clean Code';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/headphones?lock=2' WHERE name = 'Fone de Ouvido Bluetooth';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/book?lock=3' WHERE name = 'O Programador Pragmático';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/keyboard?lock=4' WHERE name = 'Teclado Mecânico';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/cookware?lock=5' WHERE name = 'Jogo de Panelas Antiaderente';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/mouse,computer?lock=6' WHERE name = 'Mouse Sem Fio';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/drawer?lock=7' WHERE name = 'Organizador de Gaveta';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/smartwatch?lock=8' WHERE name = 'Smartwatch Fit 2';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/laptop?lock=9' WHERE name = 'Notebook Dell Inspiron';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/speaker?lock=10' WHERE name = 'Caixa de Som Portátil';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/monitor?lock=11' WHERE name = 'Monitor 27" Full HD';
UPDATE products SET image_url = 'https://loremflickr.com/400/300/lamp?lock=12' WHERE name = 'Luminária de Mesa LED';
