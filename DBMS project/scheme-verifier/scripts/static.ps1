## Resolve paths relative to this script location to avoid running from wrong cwd
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$projectDir = Resolve-Path (Join-Path $scriptDir "..")

Push-Location -Path $projectDir
try {
    if (-not (Test-Path "StaticServer.class")) {
        Write-Output "Compiling StaticServer.java..."
        javac StaticServer.java
    }
    Write-Output "Starting StaticServer on port 3000..."
    java StaticServer
} finally {
    Pop-Location
}
