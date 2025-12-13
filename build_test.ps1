Set-Location "D:\codcdood\SmartExpense"
.\gradlew.bat :app:compileDebugJavaWithJavac --stacktrace 2>&1 | Out-File -FilePath "build_output.txt" -Encoding UTF8
Write-Host "Build output saved to build_output.txt"

