-- Base de datos para DL Abogados
CREATE DATABASE IF NOT EXISTS dl_abogados;
USE dl_abogados;

-- 1. Tabla: roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

-- 2. Tabla: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rol_id BIGINT NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- 3. Tabla: clientes
CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT UNIQUE NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dni VARCHAR(8) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(255),
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 4. Tabla: abogados
CREATE TABLE IF NOT EXISTS abogados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT UNIQUE NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dni VARCHAR(8) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    numero_colegiatura VARCHAR(50) UNIQUE NOT NULL,
    perfil_profesional TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 5. Tabla: especialidades
CREATE TABLE IF NOT EXISTS especialidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE
);

-- 6. Tabla: abogado_especialidad
CREATE TABLE IF NOT EXISTS abogado_especialidad (
    abogado_id BIGINT NOT NULL,
    especialidad_id BIGINT NOT NULL,
    PRIMARY KEY (abogado_id, especialidad_id),
    FOREIGN KEY (abogado_id) REFERENCES abogados(id),
    FOREIGN KEY (especialidad_id) REFERENCES especialidades(id)
);

-- 7. Tabla: servicios_legales
CREATE TABLE IF NOT EXISTS servicios_legales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    especialidad_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio_referencial DECIMAL(10,2),
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (especialidad_id) REFERENCES especialidades(id)
);

-- 8. Tabla: disponibilidad_abogado
CREATE TABLE IF NOT EXISTS disponibilidad_abogado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    abogado_id BIGINT NOT NULL,
    dia_semana VARCHAR(20) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (abogado_id) REFERENCES abogados(id)
);

-- 9. Tabla: casos
CREATE TABLE IF NOT EXISTS casos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) UNIQUE NOT NULL,
    cliente_id BIGINT NOT NULL,
    servicio_id BIGINT NOT NULL,
    caso_origen_id BIGINT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(30) NOT NULL,
    prioridad VARCHAR(20) NOT NULL,
    monto_acordado DECIMAL(10,2) NOT NULL,
    fecha_apertura DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre DATETIME NULL,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (servicio_id) REFERENCES servicios_legales(id),
    FOREIGN KEY (caso_origen_id) REFERENCES casos(id)
);

-- 10. Tabla: caso_abogado
CREATE TABLE IF NOT EXISTS caso_abogado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caso_id BIGINT NOT NULL,
    abogado_id BIGINT NOT NULL,
    fecha_asignacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_fin DATETIME NULL,
    es_responsable BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    observacion VARCHAR(500),
    FOREIGN KEY (caso_id) REFERENCES casos(id),
    FOREIGN KEY (abogado_id) REFERENCES abogados(id)
);

-- 11. Tabla: citas
CREATE TABLE IF NOT EXISTS citas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    abogado_id BIGINT NOT NULL,
    servicio_id BIGINT NULL,
    caso_id BIGINT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    modalidad VARCHAR(30) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    enlace_virtual VARCHAR(500) NULL,
    observaciones TEXT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (abogado_id) REFERENCES abogados(id),
    FOREIGN KEY (servicio_id) REFERENCES servicios_legales(id),
    FOREIGN KEY (caso_id) REFERENCES casos(id)
);

-- 12. Tabla: documentos
CREATE TABLE IF NOT EXISTS documentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caso_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    nombre_original VARCHAR(255) NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    tipo_mime VARCHAR(100),
    categoria VARCHAR(100),
    descripcion TEXT,
    visible_cliente BOOLEAN DEFAULT TRUE,
    fecha_subida DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    estado VARCHAR(50) NOT NULL DEFAULT 'RECIBIDO',
    observaciones TEXT NULL,
    version INT NOT NULL DEFAULT 1,
    FOREIGN KEY (caso_id) REFERENCES casos(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 13. Tabla: seguimiento_caso
CREATE TABLE IF NOT EXISTS seguimiento_caso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caso_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    estado_anterior VARCHAR(30) NULL,
    estado_nuevo VARCHAR(30) NULL,
    visible_cliente BOOLEAN DEFAULT TRUE,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (caso_id) REFERENCES casos(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 14. Tabla: reaperturas
CREATE TABLE IF NOT EXISTS reaperturas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caso_anterior_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    abogado_preferido_id BIGINT NULL,
    nuevo_caso_id BIGINT NULL UNIQUE,
    motivo TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL,
    observacion_respuesta TEXT NULL,
    fecha_solicitud DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_respuesta DATETIME NULL,
    FOREIGN KEY (caso_anterior_id) REFERENCES casos(id),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (abogado_preferido_id) REFERENCES abogados(id),
    FOREIGN KEY (nuevo_caso_id) REFERENCES casos(id)
);

-- 15. Tabla: pagos
CREATE TABLE IF NOT EXISTS pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caso_id BIGINT NULL,
    cita_id BIGINT NULL,
    cliente_id BIGINT NULL,
    monto DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    estado_pago VARCHAR(30) NOT NULL,
    numero_operacion VARCHAR(100) NULL,
    comprobante_url VARCHAR(500) NULL,
    fecha_pago DATETIME NULL,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    observacion VARCHAR(500) NULL,
    FOREIGN KEY (caso_id) REFERENCES casos(id),
    FOREIGN KEY (cita_id) REFERENCES citas(id),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

-- 16. Tabla: notificaciones
CREATE TABLE IF NOT EXISTS notificaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura DATETIME NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 17. Tabla: historial_actividad
CREATE TABLE IF NOT EXISTS historial_actividad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NULL,
    tipo_entidad VARCHAR(50) NOT NULL,
    entidad_id BIGINT NOT NULL,
    accion VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
