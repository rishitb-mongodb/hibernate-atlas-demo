# Reveal or re-hide the schema-flexibility block in Movie.java / Act2.java.
#
#   .\reveal.ps1 show     uncomment every //~ line inside //@REVEAL-START .. //@REVEAL-END
#   .\reveal.ps1 hide     put it back
#
# Useful live: if hand-editing goes wrong, this restores a known-good state.
param([string]$Mode)

if ($Mode -ne 'show' -and $Mode -ne 'hide') {
    Write-Host 'usage: .\reveal.ps1 {show|hide}'
    exit 2
}

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

Get-ChildItem -Path src/main/java -Filter *.java -Recurse | ForEach-Object {
    $inside = $false
    $changed = $false
    $lines = foreach ($line in [System.IO.File]::ReadAllLines($_.FullName)) {
        if ($line -match '//@REVEAL-START') { $inside = $true; $line; continue }
        if ($line -match '//@REVEAL-END') { $inside = $false; $line; continue }
        if (-not $inside) { $line; continue }
        $changed = $true
        if ($Mode -eq 'show') {
            $line -replace '//~ ?', ''
        }
        elseif ($line -match '^\s*$') { "$line//~" }
        elseif ($line -match '^\s*//~') { $line }
        else { $line -replace '^(\s*)', '$1//~ ' }
    }
    if ($changed) { [System.IO.File]::WriteAllLines($_.FullName, $lines) }
}

Write-Host $(if ($Mode -eq 'show') { 'Revealed.' } else { 'Hidden.' })
