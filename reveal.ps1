# Reveal or re-hide the demo code for one act.
#
#   .\reveal.ps1 2        uncomment every //~ line inside the //@ACT2-START .. //@ACT2-END blocks
#   .\reveal.ps1 3
#   .\reveal.ps1 reset    re-hide acts 2 and 3
#
# Useful live: if editing by hand goes wrong, this puts the file back into a known state.
param([Parameter(Mandatory = $true)][ValidateSet('2', '3', 'reset')][string]$Act)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

function Apply([string]$actNumber, [string]$mode) {
    Get-ChildItem -Path src/main/java -Filter *.java -Recurse | ForEach-Object {
        $inside = $false
        $changed = $false
        $lines = foreach ($line in [System.IO.File]::ReadAllLines($_.FullName)) {
            if ($line -match "//@ACT$actNumber-START") { $inside = $true; $line; continue }
            if ($line -match "//@ACT$actNumber-END") { $inside = $false; $line; continue }
            if (-not $inside) { $line; continue }
            $changed = $true
            if ($mode -eq 'show') {
                $line -replace '//~ ?', ''
            }
            elseif ($line -match '^\s*$') { "$line//~" }
            elseif ($line -match '^\s*//~') { $line }
            else { $line -replace '^(\s*)', '$1//~ ' }
        }
        if ($changed) { [System.IO.File]::WriteAllLines($_.FullName, $lines) }
    }
}

switch ($Act) {
    'reset' { Apply '2' 'hide'; Apply '3' 'hide'; Write-Host 'Acts 2 and 3 re-hidden.' }
    default { Apply $Act 'show'; Write-Host "Act $Act revealed." }
}
