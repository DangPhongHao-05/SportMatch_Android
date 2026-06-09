# Stage 1: Build ứng dụng
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build-env
WORKDIR /App

# Copy file csproj và restore
COPY *.csproj ./
RUN dotnet restore

# Copy toàn bộ code và build
COPY . ./
RUN dotnet publish -c Release -o out

# Stage 2: Chạy ứng dụng
FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /App
COPY --from=build-env /App/out .

# Cấu hình port 8080 cho Fly.dev
ENV ASPNETCORE_URLS=http://+:8080
EXPOSE 8080

# 🟢 THAY TÊN FILE DLL CỦA HÀO VÀO ĐÂY
ENTRYPOINT ["dotnet", "SportMatchAPI.dll"]