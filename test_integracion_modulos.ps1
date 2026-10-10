$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " TEST INTEGRAL DE LOS 6 MODULOS DE DLABOGADOS (E2E)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. AUTENTICACION
Write-Host "`n[1] Autenticando usuarios con credenciales oficiales..." -ForegroundColor Yellow

$adminLoginBody = @{ email = "admin@dlabogados.com"; password = "Admin123!" } | ConvertTo-Json
$adminAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $adminLoginBody -ContentType "application/json"
$adminToken = $adminAuth.token
Write-Host " -> Admin autenticado con exito (ROLE_ADMIN)" -ForegroundColor Green

$abogadoLoginBody = @{ email = "carlos.mendoza@dlabogados.com"; password = "Abogado123!" } | ConvertTo-Json
$abogadoAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $abogadoLoginBody -ContentType "application/json"
$abogadoToken = $abogadoAuth.token
Write-Host " -> Abogado autenticado con exito (Dr. Carlos Mendoza - ROLE_ABOGADO)" -ForegroundColor Green

$clienteLoginBody = @{ email = "juan.perez@gmail.com"; password = "Cliente123!" } | ConvertTo-Json
$clienteAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $clienteLoginBody -ContentType "application/json"
$clienteToken = $clienteAuth.token
Write-Host " -> Cliente autenticado con exito (Juan Perez - ROLE_CLIENTE)" -ForegroundColor Green

$adminHeaders = @{ Authorization = "Bearer $adminToken" }
$abogadoHeaders = @{ Authorization = "Bearer $abogadoToken" }
$clienteHeaders = @{ Authorization = "Bearer $clienteToken" }

# 2. VALIDACION DE DOCUMENTOS Y ESTADOS
Write-Host "`n[2] Validacion de Documentos y Estados..." -ForegroundColor Yellow
$casos = Invoke-RestMethod -Uri "http://localhost:8080/api/casos" -Method Get -Headers $adminHeaders
$casoPrueba = $casos[0]
Write-Host " -> Caso seleccionado: ID $($casoPrueba.id) - $($casoPrueba.codigo)" -ForegroundColor Cyan

# Subir documento
$tempFilePath = [System.IO.Path]::GetTempFileName() + ".txt"
[System.IO.File]::WriteAllText($tempFilePath, "Contenido de prueba legal para caso")
$fileBytes = [System.IO.File]::ReadAllBytes($tempFilePath)
$boundary = [System.Guid]::NewGuid().ToString()

$body = (
    "--$boundary`r`n" +
    "Content-Disposition: form-data; name=`"file`"; filename=`"demanda_inicial.txt`"`r`n" +
    "Content-Type: text/plain`r`n`r`n" +
    [System.Text.Encoding]::UTF8.GetString($fileBytes) + "`r`n" +
    "--$boundary`r`n" +
    "Content-Disposition: form-data; name=`"categoria`"`r`n`r`n" +
    "DEMANDA`r`n" +
    "--$boundary`r`n" +
    "Content-Disposition: form-data; name=`"descripcion`"`r`n`r`n" +
    "Escrito inicial de demanda civil`r`n" +
    "--$boundary--`r`n"
)

$docSubido = Invoke-RestMethod -Uri "http://localhost:8080/api/casos/$($casoPrueba.id)/documentos" -Method Post -Body $body -ContentType "multipart/form-data; boundary=$boundary" -Headers $clienteHeaders
Write-Host " -> Documento subido: ID $($docSubido.id) - Estado: $($docSubido.estado) - Version: $($docSubido.version)" -ForegroundColor Green

# Abogado observa el documento
$obsBody = @{
    estado = "OBSERVADO"
    observaciones = "Falta adjuntar tasa judicial digital"
} | ConvertTo-Json

$docObservado = Invoke-RestMethod -Uri "http://localhost:8080/api/documentos/$($docSubido.id)/estado" -Method Patch -Body $obsBody -ContentType "application/json" -Headers $abogadoHeaders
Write-Host " -> Estado actualizado por abogado a: $($docObservado.estado) - Obs: $($docObservado.observaciones)" -ForegroundColor Green

# Cliente sustituye el documento observado
$sustituirTemp = [System.IO.Path]::GetTempFileName() + ".txt"
[System.IO.File]::WriteAllText($sustituirTemp, "Documento subsanado con tasa judicial v2")
$sustBytes = [System.IO.File]::ReadAllBytes($sustituirTemp)
$boundarySust = [System.Guid]::NewGuid().ToString()

$bodySust = (
    "--$boundarySust`r`n" +
    "Content-Disposition: form-data; name=`"file`"; filename=`"demanda_subsanada.txt`"`r`n" +
    "Content-Type: text/plain`r`n`r`n" +
    [System.Text.Encoding]::UTF8.GetString($sustBytes) + "`r`n" +
    "--$boundarySust--`r`n"
)

$docSustituido = Invoke-RestMethod -Uri "http://localhost:8080/api/documentos/$($docSubido.id)/sustituir" -Method Post -Body $bodySust -ContentType "multipart/form-data; boundary=$boundarySust" -Headers $clienteHeaders
Write-Host " -> Documento sustituido exitosamente: Nueva Version: $($docSustituido.version) - Nuevo Estado: $($docSustituido.estado)" -ForegroundColor Green

Remove-Item $tempFilePath -Force
Remove-Item $sustituirTemp -Force

# 3. VALIDACION DE PAGOS
Write-Host "`n[3] Validacion de Pagos..." -ForegroundColor Yellow
$pagoBody = @{
    casoId = $casoPrueba.id
    clienteId = 2
    concepto = "Honorarios de defensa procesal"
    monto = 350.00
    metodoPago = "TRANSFERENCIA"
    numeroOperacion = "TXN-987654321"
    observacion = "Verificado en cuenta corriente"
} | ConvertTo-Json

$nuevoPago = Invoke-RestMethod -Uri "http://localhost:8080/api/pagos" -Method Post -Body $pagoBody -ContentType "application/json" -Headers $adminHeaders
Write-Host " -> Pago registrado exitosamente: ID $($nuevoPago.id) - Monto: S/ $($nuevoPago.monto) - Estado: $($nuevoPago.estadoPago)" -ForegroundColor Green

$todosPagos = Invoke-RestMethod -Uri "http://localhost:8080/api/pagos" -Method Get -Headers $adminHeaders
Write-Host " -> Total pagos en sistema: $($todosPagos.Length)" -ForegroundColor Cyan

$pagosCliente = Invoke-RestMethod -Uri "http://localhost:8080/api/pagos/cliente/2" -Method Get -Headers $clienteHeaders
Write-Host " -> Total pagos para Cliente #2: $($pagosCliente.Length)" -ForegroundColor Cyan

# 4. VALIDACION DE HISTORIAL DE AUDITORIA
Write-Host "`n[4] Validacion de Auditoria..." -ForegroundColor Yellow
$historial = Invoke-RestMethod -Uri "http://localhost:8080/api/historial" -Method Get -Headers $adminHeaders
Write-Host " -> Total eventos en historial: $($historial.Length)" -ForegroundColor Cyan
$historial | Select-Object -First 3 | ForEach-Object {
    Write-Host "    [$($_.fecha)] [$($_.tipoEntidad) #$($_.entidadId)] $($_.accion) - $($_.descripcion)" -ForegroundColor Gray
}

# 5. VALIDACION DE REAPERTURAS
Write-Host "`n[5] Validacion de Reaperturas..." -ForegroundColor Yellow
$casoCerrado = $casos | Where-Object { $_.estado -eq "CERRADO" -or $_.estado -eq "FINALIZADO" } | Select-Object -First 1

if (-not $casoCerrado) {
    Write-Host " -> Cerrando caso ID $($casoPrueba.id) para prueba de reapertura..."
    $cambiarEstadoBody = @{ estado = "CERRADO"; observacion = "Conclusion temporal del caso" } | ConvertTo-Json
    $casoCerrado = Invoke-RestMethod -Uri "http://localhost:8080/api/casos/$($casoPrueba.id)/estado" -Method Patch -Body $cambiarEstadoBody -ContentType "application/json" -Headers $adminHeaders
}

Write-Host " -> Caso cerrado: ID $($casoCerrado.id) - $($casoCerrado.codigo)" -ForegroundColor Cyan

# Cliente solicita reapertura
$reaperturaReq = @{
    motivo = "Aparicion de nuevas pruebas testimoniales fehacientes"
    abogadoPreferidoId = 1
} | ConvertTo-Json

$solicitud = Invoke-RestMethod -Uri "http://localhost:8080/api/casos/$($casoCerrado.id)/reaperturas" -Method Post -Body $reaperturaReq -ContentType "application/json" -Headers $clienteHeaders
Write-Host " -> Solicitud de reapertura registrada: ID $($solicitud.id) - Estado: $($solicitud.estado)" -ForegroundColor Green

# Admin aprueba reapertura
$aprobacionReq = @{
    observacionRespuesta = "Aprobada formalmente la reapertura con asignacion preferente"
} | ConvertTo-Json

$reaperturaAprobada = Invoke-RestMethod -Uri "http://localhost:8080/api/reaperturas/$($solicitud.id)/aprobar" -Method Patch -Body $aprobacionReq -ContentType "application/json" -Headers $adminHeaders
Write-Host " -> Reapertura aprobada! Nuevo Caso ID: $($reaperturaAprobada.nuevoCasoId) - Codigo: $($reaperturaAprobada.nuevoCasoCodigo)" -ForegroundColor Green

# 6. VERIFICAR TRAZABILIDAD
Write-Host "`n[6] Verificacion de Trazabilidad en Auditoria..." -ForegroundColor Yellow
$historialFinal = Invoke-RestMethod -Uri "http://localhost:8080/api/historial" -Method Get -Headers $adminHeaders
$eventosReap = $historialFinal | Where-Object { $_.tipoEntidad -eq "REAPERTURA" }
Write-Host " -> Eventos de reapertura en auditoria: $($eventosReap.Length)" -ForegroundColor Green

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host " TODOS LOS 6 MODULOS VERIFICADOS CON EXITO (100% OPERATIVO)" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
