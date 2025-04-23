@echo off
setlocal enabledelayedexpansion

:: Initialize totals
set "lines=0"
set "words=0"
set "chars=0"

:: Loop through all .java files recursively
for /R %%f in (*.java) do (
    for /f "usebackq delims=" %%a in ("%%f") do (
        set /a lines+=1

        set "line=%%a"

        :: Count characters
        set "len=0"
        call :strlen line len
        set /a chars+=len

        :: Count words
        for %%w in (%%a) do (
            set /a words+=1
        )
    )
)

:: Output results with separator
echo ===========================
echo Total lines     : %lines%
echo Total words     : %words%
echo Total characters: %chars%
echo ===========================

:: Keep window open
echo.
echo Press any key to exit...
pause >nul
goto :eof

:: Function to get string length
:strlen
setlocal EnableDelayedExpansion
set "s=!%1!"
set "len=0"
:loop
if defined s (
    set "s=!s:~1!"
    set /a len+=1
    goto loop
)
endlocal & set "%2=%len%"
exit /b
