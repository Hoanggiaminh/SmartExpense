Set-Location "D:\codcdood\SmartExpense"
Write-Host "Building project after simplifying Category Management..."
.\gradlew.bat :app:assembleDebug --stacktrace 2>&1 | Tee-Object -FilePath "build_output.txt"
Write-Host ""
Write-Host "Build completed. Check build_output.txt for details."

