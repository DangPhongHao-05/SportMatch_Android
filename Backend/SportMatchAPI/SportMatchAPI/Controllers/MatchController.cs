using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using NetTopologySuite.Geometries;
using SportMatchAPI.Data;
using SportMatchAPI.DTO;
using SportMatchAPI.Models;

namespace SportMatchAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MatchController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly GeometryFactory _geometryFactory;

        public MatchController(AppDbContext context)
        {
            _context = context;
            // Khởi tạo hệ WGS84: SRID 4326
            _geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        }

        [HttpPost("nearby")]
        public async Task<IActionResult> GetNearbyMatches([FromBody] LocationQueryDTO query)
        {
            try
            {
                // 1. Tạo điểm tìm kiếm: Thống nhất trục (X = Longitude, Y = Latitude) của NetTopologySuite
                var userLocation = _geometryFactory.CreatePoint(new Coordinate(query.Longitude, query.Latitude));

                // Quy đổi bán kính từ Km sang Mét
                double radiusInMeters = query.RadiusInKm * 1000;

                // Chuẩn hóa thời gian hiện tại theo múi giờ Việt Nam (GMT+7)
                var utcNow = DateTime.UtcNow;
                var currentTime = TimeZoneInfo.ConvertTimeFromUtc(utcNow, TimeZoneInfo.FindSystemTimeZoneById("SE Asia Standard Time"));

                // 2. Lấy danh sách các trận đang Open từ DB lên RAM trước để tránh EF Core dịch sai hàm Distance
                var allOpenMatches = await _context.Matchrequests
                    .Include(m => m.Host)
                    .Where(m => m.Status == "Open" && m.EndTime > currentTime)
                    .Select(m => new
                    {
                        m.Id,
                        m.HostId,
                        HostName = m.Host.FullName,
                        HostPhone = m.Host.PhoneNumber,
                        m.SportType,
                        m.RequestType,
                        m.MissingPlayers,
                        m.Description,
                        m.StartTime,
                        m.EndTime,
                        m.Location // Lấy đối tượng hình học gốc lên để C# tính toán
                    })
                    .ToListAsync();

                // 3. Sử dụng C# để tính toán khoảng cách chuẩn xác trên RAM
                var nearbyMatches = allOpenMatches
                    .Select(m => {
                        // Tính khoảng cách phẳng (Độ hình học) giữa 2 điểm trên RAM
                        double distanceInDegrees = m.Location.Distance(userLocation);
                        // Quy đổi ra mét chuẩn
                        double distanceInMeters = Math.Round(distanceInDegrees * 111000);

                        return new
                        {
                            m.Id,
                            m.HostId,
                            m.HostName,
                            m.HostPhone,
                            m.SportType,
                            m.RequestType,
                            m.MissingPlayers,
                            m.Description,
                            m.StartTime,
                            m.EndTime,

                            // Trả dữ liệu về đúng trục cho Android nhận diện
                            Latitude = m.Location.Y,  
                            Longitude = m.Location.X,
                            DistanceInMeters = distanceInMeters
                        };
                    })
                    // LỌC CHÍNH XÁC THEO BÁN KÍNH MÉT TẠI ĐÂY
                    .Where(m => m.DistanceInMeters <= radiusInMeters)
                    .ToList();

                // Trả kết quả về dạng Ok Object chuẩn IActionResult
                return Ok(nearbyMatches);
            }
            catch (Exception ex)
            {
                return StatusCode(500, $"Lỗi quét vị trí hệ thống: {ex.Message}");
            }
        }

        [HttpPost("create")]
        public async Task<IActionResult> CreateMatch([FromBody] CreateMatchDto dto)
        {
            try
            {
                // Lúc tạo kèo: Giữ nguyên cấu trúc truyền Longitude trước, Latitude sau để lưu đúng dạng POINT(13.xxxx 109.xxxx)
                var pointLocation = _geometryFactory.CreatePoint(new Coordinate(dto.Longitude, dto.Latitude));

                var newMatch = new Matchrequest
                {
                    HostId = dto.HostId,
                    SportType = dto.SportType,
                    RequestType = dto.RequestType,
                    MissingPlayers = dto.MissingPlayers,
                    Description = dto.Description,
                    StartTime = dto.StartTime,
                    EndTime = dto.EndTime,
                    Location = pointLocation,
                    Status = "Open",
                    CreatedAt = DateTime.UtcNow
                };

                _context.Matchrequests.Add(newMatch);
                await _context.SaveChangesAsync();

                return Ok(new { message = "Tạo kèo thành công!" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi hệ thống server: " + ex.Message });
            }
        }
    }
}