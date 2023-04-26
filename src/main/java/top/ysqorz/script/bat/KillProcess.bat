@echo off & setlocal EnableDelayedExpansion
CHCP 65001
CLS
echo 请输入程序正在运行的端口号
set /p port=
echo 找到的进程记录
echo =================================================================================
netstat -nao|findstr !port!
echo =================================================================================
echo 回车进行逐个确认
pause
for /f "tokens=2,5" %%i in ('netstat -nao^|findstr :%%port%%') do (
	::if "!processed[%%j]!" == "" (
	if not defined processed[%%j] (
		set pname=N/A
		for /f "tokens=1" %%p in ('tasklist^|findstr %%j') do (set pname=%%p)
		echo %%i	%%j	!pname!
		echo 输入Y确认Kill，否则跳过，可回车跳过
		set flag=N/A
		set /p flag=
		if "!flag!" == "Y" (
			taskkill /pid %%j -t -f
		) else (
			echo 跳过
		)
		set processed[%%j]=1
	)
)
echo 程序结束
pause
