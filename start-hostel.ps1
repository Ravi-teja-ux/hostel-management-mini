$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$frontendUrl = "http://127.0.0.1:5500/index.html"
$dashboardUrl = "http://localhost:8080/api/admin/dashboard"
$javaHome = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$mavenHome = Join-Path $env:USERPROFILE "scoop\apps\maven\current"

function Test-HttpEndpoint([string]$Url) {
    try {
        Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 3 | Out-Null
        return $true
    } catch {
        return $false
    }
}

function Test-Port([int]$Port) {
    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

if (-not (Test-Path (Join-Path $projectRoot "frontend\index.html"))) {
    throw "The frontend was not found under $projectRoot."
}
if (-not (Test-Path (Join-Path $projectRoot "backend\pom.xml"))) {
    throw "The backend was not found under $projectRoot."
}
if (-not (Test-Path (Join-Path $javaHome "bin\java.exe"))) {
    throw "Java 17 was not found at $javaHome. Install Java 17, then try again."
}
if (-not (Test-Path (Join-Path $mavenHome "bin\mvn.cmd"))) {
    throw "Maven was not found at $mavenHome. Install Maven, then try again."
}
if (-not (Get-Command npx.cmd -ErrorAction SilentlyContinue)) {
    throw "Node.js and npm are required to start the frontend. Install Node.js LTS, then try again."
}

$frontendReady = Test-HttpEndpoint $frontendUrl
if (-not $frontendReady -and (Test-Port 5500)) {
    throw "Port 5500 is already used by another service. Close that service and run this launcher again."
}

$backendReady = Test-HttpEndpoint $dashboardUrl
if (-not $backendReady -and (Test-Port 8080)) {
    throw "Port 8080 is already used by another service. Close that service and run this launcher again."
}

if (-not $backendReady) {
    if (-not (Test-Port 3306)) {
        throw "MySQL is not listening on port 3306. Start the MySQL service, then run this launcher again."
    }

    if ([string]::IsNullOrWhiteSpace($env:DB_USERNAME)) {
        $env:DB_USERNAME = "root"
    }
    if ([string]::IsNullOrEmpty($env:DB_PASSWORD)) {
        $securePassword = Read-Host "Enter your MySQL password (input is hidden)" -AsSecureString
        $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try {
            $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
        } finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
            $securePassword.Dispose()
        }
    }

    $env:JAVA_HOME = $javaHome
    $env:Path = "$javaHome\bin;$mavenHome\bin;$env:Path"
    Start-Process -FilePath (Join-Path $PSHOME "powershell.exe") `
        -WorkingDirectory (Join-Path $projectRoot "backend") `
        -ArgumentList @("-NoExit", "-NoProfile", "-Command", "mvn spring-boot:run") | Out-Null
    Write-Host "Starting Spring Boot. Its console will show startup errors if the database credentials are incorrect."
}

if (-not $frontendReady) {
    Start-Process -FilePath (Join-Path $PSHOME "powershell.exe") `
        -WorkingDirectory (Join-Path $projectRoot "frontend") `
        -ArgumentList @("-NoExit", "-NoProfile", "-Command", "npx --yes http-server . -p 5500 -a 127.0.0.1 -c-1") | Out-Null
    Write-Host "Starting the frontend."
}

if (-not $backendReady) {
    Write-Host "Waiting for the backend API..."
    for ($attempt = 0; $attempt -lt 60 -and -not $backendReady; $attempt++) {
        Start-Sleep -Seconds 2
        $backendReady = Test-HttpEndpoint $dashboardUrl
    }
    if (-not $backendReady) {
        throw "The backend did not become ready. Check the Spring Boot console; MySQL credentials may be incorrect."
    }
}

if (-not $frontendReady) {
    Write-Host "Waiting for the frontend..."
    for ($attempt = 0; $attempt -lt 20 -and -not $frontendReady; $attempt++) {
        Start-Sleep -Seconds 1
        $frontendReady = Test-HttpEndpoint $frontendUrl
    }
    if (-not $frontendReady) {
        throw "The frontend did not become ready. Check its console window for errors."
    }
}

Write-Host ""
Write-Host "Hostel Management System is ready: $frontendUrl"
Write-Host "Backend API is responding at http://localhost:8080."
Start-Process $frontendUrl
