@echo off
setlocal enabledelayedexpansion

rem 检查是否传递了端口号
if "%1"=="" (
    echo 请输入端口号。
    exit /b 1
)

rem 查找占用指定端口号的进程ID
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%1') do (
    set PID=%%a
    echo 找到占用端口 %1 的进程ID：!PID!
)

rem 检查是否找到了进程ID
if "!PID!"=="" (
    echo 未找到占用端口 %1 的进程。
    exit /b 1
)

rem 强制终止该进程
taskkill /f /pid !PID!

echo 端口 %1 对应的进程已终止。
