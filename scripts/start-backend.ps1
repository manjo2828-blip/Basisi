# Basisi 백엔드를 OpenAI API 키와 함께 실행합니다.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$secrets = Join-Path $PSScriptRoot "local-secrets.ps1"

if (Test-Path $secrets) {
    . $secrets
    Write-Host "[start-backend] loaded local-secrets.ps1"
} else {
    Write-Host "[start-backend] local-secrets.ps1 not found. Set BASISI_OPENAI_API_KEY manually or copy local-secrets.example.ps1"
}

if (-not $env:BASISI_OPENAI_API_KEY) {
    Write-Warning "BASISI_OPENAI_API_KEY is empty. OpenAI LLM will use algorithm fallback only."
} else {
    Write-Host "[start-backend] BASISI_OPENAI_API_KEY is set."
}

Set-Location $root
try {
    & (Join-Path $root "scripts\free-port-8080.ps1")
} catch {
    Write-Host "[start-backend] free-port-8080 skipped: $($_.Exception.Message)"
}
& (Join-Path $root "gradlew.bat") bootRun
