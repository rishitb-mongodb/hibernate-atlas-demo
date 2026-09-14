@echo off
rem Reveal or re-hide the schema-flexibility block. Works from cmd.exe, PowerShell, or a
rem double-click in Explorer -- unlike reveal.ps1, which Windows opens in a text editor by
rem default instead of running, and which PowerShell may also block via execution policy.
rem
rem   reveal.cmd show
rem   reveal.cmd hide
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0reveal.ps1" %*
