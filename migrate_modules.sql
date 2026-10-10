-- Migración para integración de módulos: Documentos y Pagos
USE dl_abogados;

-- 1. Actualización tabla documentos
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'dl_abogados' AND table_name = 'documentos' AND column_name = 'estado');
SET @query = IF(@col_exists = 0, 'ALTER TABLE documentos ADD COLUMN estado VARCHAR(50) NOT NULL DEFAULT ''RECIBIDO''', 'SELECT 1');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'dl_abogados' AND table_name = 'documentos' AND column_name = 'observaciones');
SET @query = IF(@col_exists = 0, 'ALTER TABLE documentos ADD COLUMN observaciones TEXT NULL', 'SELECT 1');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'dl_abogados' AND table_name = 'documentos' AND column_name = 'version');
SET @query = IF(@col_exists = 0, 'ALTER TABLE documentos ADD COLUMN version INT NOT NULL DEFAULT 1', 'SELECT 1');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. Actualización tabla pagos
ALTER TABLE pagos MODIFY COLUMN caso_id BIGINT NULL;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'dl_abogados' AND table_name = 'pagos' AND column_name = 'cita_id');
SET @query = IF(@col_exists = 0, 'ALTER TABLE pagos ADD COLUMN cita_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'dl_abogados' AND table_name = 'pagos' AND column_name = 'cliente_id');
SET @query = IF(@col_exists = 0, 'ALTER TABLE pagos ADD COLUMN cliente_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @query; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Actualizar pagos existentes que no tengan cliente_id asignándoles el cliente de su caso
UPDATE pagos p JOIN casos c ON p.caso_id = c.id SET p.cliente_id = c.cliente_id WHERE p.cliente_id IS NULL;
