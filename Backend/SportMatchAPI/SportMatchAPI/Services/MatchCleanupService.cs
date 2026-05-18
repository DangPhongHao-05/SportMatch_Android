using Microsoft.EntityFrameworkCore;
using SportMatchAPI.Data;

namespace SportMatchAPI.Services
{
    public class MatchCleanupService : BackgroundService
    {
        private readonly IServiceScopeFactory _scopeFactory;
        private readonly ILogger<MatchCleanupService> _logger;

        public MatchCleanupService(IServiceScopeFactory scopeFactory, ILogger<MatchCleanupService> logger)
        {
            _scopeFactory = scopeFactory;
            _logger = logger;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            // Vòng lặp chạy vô hạn song song với ứng dụng
            while (!stoppingToken.IsCancellationRequested)
            {
                _logger.LogInformation("Đang quét dọn các kèo thể thao quá hạn...");

                try
                {
                    using (var scope = _scopeFactory.CreateScope())
                    {
                        var context = scope.ServiceProvider.GetRequiredService<AppDbContext>();

                        // Sử dụng DateTime.Now (Giờ hệ thống Việt Nam) đồng bộ với DB
                        var now = DateTime.Now;

                        // 🔥 LOGIC CHUẨN ĐÉT: Kèo chỉ Expired khi thời gian hiện tại ĐÃ VƯỢT QUÁ GIỜ KẾT THÚC (EndTime)
                        var expiredMatches = await context.Matchrequests
                            .Include(m => m.Matchinteractions)
                            .Where(m => m.Status == "Open"
                                        && m.EndTime <= now // <-- Thay StartTime bằng EndTime ở đây!
                                        && !m.Matchinteractions.Any())
                            .ToListAsync(stoppingToken);

                        if (expiredMatches.Any())
                        {
                            foreach (var match in expiredMatches)
                            {
                                match.Status = "Expired"; // Tự động đóng kèo khi thực sự hết giờ rảnh
                            }

                            await context.SaveChangesAsync(stoppingToken);
                            _logger.LogInformation($"Đã tự động hủy {expiredMatches.Count} kèo quá hạn kết thúc không có người inbox.");
                        }
                    }
                }
                catch (Exception ex)
                {
                    _logger.LogError($"Lỗi trong quá trình quét dọn kèo: {ex.Message}");
                }

                // Cứ mỗi 1 phút hệ thống sẽ tự động quét kiểm tra database một lần
                await Task.Delay(TimeSpan.FromMinutes(1), stoppingToken);
            }
        }
    }
}