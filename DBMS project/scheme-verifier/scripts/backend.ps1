param(
    [string]$action = "run"
)

## Resolve paths relative to this script location to avoid running from wrong cwd
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$projectDir = Resolve-Path (Join-Path $scriptDir "..")
$backendDir = Join-Path $projectDir "backend"
$libDir = Join-Path $projectDir "lib"

# Find the MySQL connector jar inside the project's lib directory
$jarItem = Get-ChildItem (Join-Path $libDir "mysql-connector*.jar") -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $jarItem) {
    Write-Error "MySQL connector jar not found in $libDir. Place the connector jar like mysql-connector-j.jar in scheme-verifier/lib."
    exit 1
}
$jar = $jarItem.FullName

Push-Location -Path $backendDir
try {
    switch ($action.ToLower()) {
        "compile" {
            Write-Output "Using jar: $jar"
            javac -cp "$jar;." *.java
            Write-Output "Compiled backend sources."
            break
        }
        "dbreset" {
            Write-Output "Running DBReset..."
            java -cp "$jar;$projectDir" backend.DBReset
            break
        }
        default {
            # run
            if (-not $env:DB_URL) { $env:DB_URL = "jdbc:mysql://localhost:3306/scheme_verifier_db" }
            if (-not $env:DB_USER) { $env:DB_USER = "root" }
            if (-not $env:DB_PASSWORD) { $env:DB_PASSWORD = "root" }
            Write-Output "Starting backend (Main) with DB_URL=$($env:DB_URL)"
            java -cp "$jar;$projectDir" backend.Main
            break
        }
    }
} finally {
    Pop-Location
}
