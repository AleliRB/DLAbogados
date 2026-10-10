try {
    $body = Get-Content -Path "login_test.json" -Raw
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body $body
    Write-Host "SUCCESS: " ($res | ConvertTo-Json -Depth 5)
} catch {
    Write-Host "ERROR: " $_.Exception.Message
}
