@echo off
REM Script para configurar JAVA_HOME y actualizar el Path para JDK 17

SETX JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot" /M

REM Añade JAVA_HOME\bin al Path solo si no existe ya
SET "_search=%JAVA_HOME%\bin"
ECHO %PATH% | FIND /I "%_search%" >NUL
IF ERRORLEVEL 1 (
    SETX PATH "%PATH%;%JAVA_HOME%\bin" /M
)

ECHO JAVA_HOME y Path configurados. Cierra y abre la terminal para que los cambios surtan efecto.
PAUSE

