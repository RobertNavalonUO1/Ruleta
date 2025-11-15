@echo off
rem Script para compilar el módulo app en Windows (usa gradlew.bat)
cd /d "%~dp0\.."
call gradlew.bat :app:assembleDebug --stacktrace

