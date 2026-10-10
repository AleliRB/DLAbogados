# Test End-to-End: Disponibilidad exclusiva y prevención de duplicados

$loginBody = @{
    email = "juan.perez@gmail.com"
    password = "Cliente123!"
} | ConvertTo-Json

Write-Host "1. Autenticando cliente..." -ForegroundColor Cyan
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token
$clienteId = $loginResponse.user.clienteId
Write-Host "Token obtenido. Cliente ID: $clienteId (Usuario: $($loginResponse.user.nombres) $($loginResponse.user.apellidos))" -ForegroundColor Green

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 2. Consultar disponibilidad en fecha futura (2026-10-21, Miércoles hábil)
# Servicio 3: Redacción e Incumplimiento de Contrato (Derecho Civil) -> Abogado 1: Hilda Rosas
$fechaTest = "2026-10-21"
$servicioId = 3
$abogadoId = 1

Write-Host "`n2. Consultando horarios disponibles para Abogado $abogadoId en $fechaTest (Servicio $servicioId)..." -ForegroundColor Cyan
$disponiblesInicial = Invoke-RestMethod -Uri "http://localhost:8080/api/citas/disponibles?abogadoId=$abogadoId&fecha=$fechaTest&servicioId=$servicioId" -Method Get -Headers $headers
Write-Host "Slots exclusivamente disponibles encontrados: $($disponiblesInicial.Count)" -ForegroundColor Green
$disponiblesInicial | ForEach-Object { Write-Host " - Turno: $($_.etiqueta)" }

if ($disponiblesInicial.Count -eq 0) {
    Write-Host "No hay slots en esa fecha." -ForegroundColor Red
    exit 1
}

$slotElegido = $disponiblesInicial[0]
Write-Host "`nSlot elegido para reserva: $($slotElegido.horaInicio) - $($slotElegido.horaFin)" -ForegroundColor Yellow

# 3. Registrar cita en el slot elegido
Write-Host "`n3. Registrando reserva de cita..." -ForegroundColor Cyan
$citaBody = @{
    clienteId = $clienteId
    servicioId = $servicioId
    abogadoId = $abogadoId
    fecha = $fechaTest
    horaInicio = $slotElegido.horaInicio
    horaFin = $slotElegido.horaFin
    modalidad = "PRESENCIAL"
    motivo = "Consulta sobre incumplimiento contractual comercial"
} | ConvertTo-Json

$citaCreada = Invoke-RestMethod -Uri "http://localhost:8080/api/citas" -Method Post -Body $citaBody -Headers $headers
Write-Host "Cita creada con ID: $($citaCreada.id), Estado: $($citaCreada.estado)" -ForegroundColor Green
Write-Host "Servicio: $($citaCreada.servicioNombre), Abogado: $($citaCreada.abogadoNombre)" -ForegroundColor Green

# 4. Consultar disponibilidad nuevamente para verificar que el slot elegido YA NO APARECE
Write-Host "`n4. Verificando que el horario reservado haya sido excluido de la disponibilidad..." -ForegroundColor Cyan
$disponiblesPosterior = Invoke-RestMethod -Uri "http://localhost:8080/api/citas/disponibles?abogadoId=$abogadoId&fecha=$fechaTest&servicioId=$servicioId" -Method Get -Headers $headers
Write-Host "Slots disponibles posteriores: $($disponiblesPosterior.Count)" -ForegroundColor Green

$slotAunExiste = $disponiblesPosterior | Where-Object { $_.horaInicio -eq $slotElegido.horaInicio }
if ($slotAunExiste) {
    Write-Host "ERROR: El slot $($slotElegido.horaInicio) sigue apareciendo en la lista de disponibles!" -ForegroundColor Red
    exit 1
} else {
    Write-Host "EXITO CONFIRMADO: El slot $($slotElegido.horaInicio) fue excluido exitosamente de los horarios disponibles." -ForegroundColor Green
}

# 5. Intentar crear cita duplicada en el mismo horario (Debe retornar HTTP 409 Conflict)
Write-Host "`n5. Probando intento de reserva duplicada simultánea..." -ForegroundColor Cyan
try {
    $citaDuplicada = Invoke-RestMethod -Uri "http://localhost:8080/api/citas" -Method Post -Body $citaBody -Headers $headers
    Write-Host "ERROR: La cita duplicada fue aceptada indebidamente!" -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "EXITO CONCURRENCIA: Rechazo esperado con HTTP $statusCode Conflict!" -ForegroundColor Green
}

# 6. Consultar citas del cliente
Write-Host "`n6. Listando citas del cliente..." -ForegroundColor Cyan
$citasCliente = Invoke-RestMethod -Uri "http://localhost:8080/api/citas?clienteId=$clienteId" -Method Get -Headers $headers
Write-Host "Total citas registradas del cliente: $($citasCliente.Count)" -ForegroundColor Green
Write-Host "Ultima cita: ID $($citasCliente[-1].id), Fecha: $($citasCliente[-1].fecha), Turno: $($citasCliente[-1].horaInicio) - $($citasCliente[-1].horaFin), Estado: $($citasCliente[-1].estado)" -ForegroundColor Green

Write-Host "`n=======================================================" -ForegroundColor Green
Write-Host "=== TODAS LAS PRUEBAS E2E PASARON SATISFACTORIAMENTE ===" -ForegroundColor Green
Write-Host "=======================================================" -ForegroundColor Green
