using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;
using Microsoft.EntityFrameworkCore;
using SportMatchAPI.Data;
using SportMatchAPI.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
// 1. Lấy chuỗi kết nối từ appsettings.json
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");

// Khởi tạo Firebase Admin SDK
try
{
    string pathToKey = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "firebase-adminsdk.json");
    if (File.Exists(pathToKey))
    {
        FirebaseApp.Create(new AppOptions()
        {
            Credential = GoogleCredential.FromFile(pathToKey)
        });
        Console.WriteLine("==> FIREBASE ADMIN SDK KẾT NỐI THÀNH CÔNG!");
    }
}
catch (Exception ex)
{
    Console.WriteLine($"==> LỖI KHỞI TẠO FIREBASE: {ex.Message}");
    // Đừng ném lỗi ở đây để Server vẫn có thể chạy tiếp
}

// 2. Cấu hình DbContext dùng MySQL và hỗ trợ Tọa độ (Point)
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseMySql(
        connectionString,
        ServerVersion.AutoDetect(connectionString),
        mySqlOptions => mySqlOptions.UseNetTopologySuite() // Cần thiết để tính khoảng cách sân bóng
    )
);
// Đăng ký dịch vụ tự động quét dọn kèo quá hạn chạy ngầm
builder.Services.AddHostedService<MatchCleanupService>();

builder.Services.AddControllers();
// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();

app.UseStaticFiles();

app.UseAuthorization();

app.MapControllers();

app.Run();
