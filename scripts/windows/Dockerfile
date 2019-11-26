# escape=`

FROM mcr.microsoft.com/windows/servercore@sha256:d7e7e3702cbc4d8ea29001a02c1c852d0229a0679d94e017a41c43dbaa01db20

RUN @"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
RUN choco install windows-sdk-7.1 kb2519277 -y
RUN choco install graalvm --version 19.2.1 -y

CMD call scripts/windows/build-native.cmd