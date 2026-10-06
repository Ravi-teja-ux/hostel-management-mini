@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-hostel.ps1"
if errorlevel 1 (
    echo.
    echo Could not start the Hostel Management System. Read the error above.
    pause
)
