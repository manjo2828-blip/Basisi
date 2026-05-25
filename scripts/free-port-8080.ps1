# bootRun 전에 8080 LISTEN 프로세스를 종료합니다. (이전에 켜 둔 Spring/Tomcat 등)
$ErrorActionPreference = 'SilentlyContinue'
$ids = Get-NetTCPConnection -State Listen -LocalPort 8080 |
    Select-Object -ExpandProperty OwningProcess -Unique
foreach ($procId in $ids) {
    if ($procId -and $procId -ne $PID) {
        Write-Host "free-port-8080: stopping PID $procId (port 8080)"
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
    }
}
Start-Sleep -Milliseconds 400
