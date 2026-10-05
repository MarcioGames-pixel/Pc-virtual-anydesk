@echo off
setlocal EnableExtensions

where choco >nul 2>&1
if errorlevel 1 exit /b 1

choco install anydesk -y --ignore-checksums --force --no-progress

if errorlevel 1 exit /b 1

set "ANYDESK="

if exist "%ProgramFiles%\AnyDesk\AnyDesk.exe" set "ANYDESK=%ProgramFiles%\AnyDesk\AnyDesk.exe"
if not defined ANYDESK if exist "%ProgramFiles(x86)%\AnyDesk\AnyDesk.exe" set "ANYDESK=%ProgramFiles(x86)%\AnyDesk\AnyDesk.exe"
if not defined ANYDESK if exist "%ProgramData%\AnyDesk\AnyDesk.exe" set "ANYDESK=%ProgramData%\AnyDesk\AnyDesk.exe"

if not defined ANYDESK (
    for /f "delims=" %%A in ('where AnyDesk.exe 2^>nul') do (
        set "ANYDESK=%%A"
        goto :found
    )
)

:found

if not defined ANYDESK (
    for /f "delims=" %%A in ('powershell -NoProfile -Command "Get-ChildItem -Path ''C:\Program Files'',''C:\Program Files (x86)'',''C:\ProgramData'' -Filter ''AnyDesk.exe'' -Recurse -ErrorAction SilentlyContinue ^| Select-Object -First 1 -ExpandProperty FullName"') do (
        set "ANYDESK=%%A"
    )
)

if not defined ANYDESK exit /b 1

for %%A in ("%ANYDESK%") do set "ANYDESK_DIR=%%~dpA"
set "PATH=%ANYDESK_DIR%;%PATH%"

"%ANYDESK%" --version

powershell -NoProfile -Command "Invoke-WebRequest 'https://www.dropbox.com/scl/fi/cypox1i3e2ewmpnr19fqn/start.bat?rlkey=s6skd47gmo21vkl0yv3tyneri&dl=1' -OutFile 'start.bat'"

if not exist start.bat exit /b 1

python --version

if errorlevel 1 choco install python -y --no-progress

python -m pip install --upgrade pip --quiet
python -m pip install pyautogui psutil --quiet

curl -L -s "https://www.dropbox.com/scl/fi/ox42qglbf6fsnm9erf8cw/timelimit.py?rlkey=opyeqgum1k95kud81xlc7d66r&dl=1" -o time.py
curl -L -s "https://www.dropbox.com/scl/fi/lmlcrz3v1sayw8p51ki1j/shutdown.bat?rlkey=ysjxivh5vz4scnhkqgv3yc4mm&dl=1" -o shutdown.bat

curl -L -s -o "%PUBLIC%\Desktop\Telegram.exe" "https://telegram.org/dl/desktop/win64"
curl -L -s -o "%PUBLIC%\Desktop\Winrar.exe" "https://www.rarlab.com/rar/winrar-x64-621.exe"

powershell -NoProfile -Command "Invoke-WebRequest 'https://github.com/chieunhatnang/VM-QuickConfig/releases/download/1.6.1/VMQuickConfig.exe' -OutFile '%PUBLIC%\Desktop\VMQuickConfig.exe'"

if exist "%PUBLIC%\Desktop\Telegram.exe" (
    "%PUBLIC%\Desktop\Telegram.exe" /VERYSILENT /NORESTART
    del /f /q "%PUBLIC%\Desktop\Telegram.exe"
)

if exist "%PUBLIC%\Desktop\Winrar.exe" (
    "%PUBLIC%\Desktop\Winrar.exe" /S
    del /f /q "%PUBLIC%\Desktop\Winrar.exe"
)

del /f /q "%PUBLIC%\Desktop\Epic Games Launcher.lnk" >nul 2>&1
del /f /q "%PUBLIC%\Desktop\Unity Hub.lnk" >nul 2>&1

set "password=@#Joyzonetech123456"

powershell -NoProfile -Command "$p = ConvertTo-SecureString '%password%' -AsPlainText -Force; Set-LocalUser -Name 'runneradmin' -Password $p"

reg add "HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\HideDesktopIcons\NewStartPanel" /v "{20D04FE0-3AEA-1069-A2D8-08002B30309D}" /t REG_DWORD /d 0 /f

tzutil /s "Pacific Standard Time"

start "" "%ANYDESK%"

timeout /t 10 /nobreak >nul

tasklist /FI "IMAGENAME eq AnyDesk.exe" | find /I "AnyDesk.exe" >nul

if errorlevel 1 exit /b 1

exit /b 0
