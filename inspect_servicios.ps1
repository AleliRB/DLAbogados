$loginBody = @{
    email = "juan.perez@gmail.com"
    password = "Cliente123!"
} | ConvertTo-Json

$loginRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginRes.token
$headers = @{ "Authorization" = "Bearer $token" }

Write-Host "--- SERVICIOS LEGALES ACTIVOS ---"
$servicios = Invoke-RestMethod -Uri "http://localhost:8080/api/servicios/activos" -Method Get -Headers $headers
$servicios | ForEach-Object {
    Write-Host "`nServicio ID: $($_.id) - $($_.nombre) (Especialidad: $($_.especialidadNombre), ID: $($_.especialidadId))"
    
    # Abogados para este servicio
    $abogados = Invoke-RestMethod -Uri "http://localhost:8080/api/abogados/servicio/$($_.id)" -Method Get -Headers $headers
    Write-Host "  Abogados disponibles: $($abogados.Count)"
    $abogados | ForEach-Object {
        Write-Host "   -> Abogado ID: $($_.id) - Dr(a). $($_.nombres) $($_.apellidos)"
    }
}
