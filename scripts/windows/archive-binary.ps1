$version = Get-Content .\VERSION -Raw
mkdir releases
Compress-Archive -Path titan.exe -CompressionLevel Optimal -DestinationPath releases/titan-$version-windows_amd64.zip