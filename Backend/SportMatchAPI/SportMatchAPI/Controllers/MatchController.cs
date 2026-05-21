using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using NetTopologySuite.Geometries;
using SportMatchAPI.Data;
using SportMatchAPI.DTO;
using SportMatchAPI.Models;
using FirebaseAdmin.Messaging;
using FcmMessage = FirebaseAdmin.Messaging.Message;
using FcmNotification = FirebaseAdmin.Messaging.Notification;

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

        // gửi yêu cầu tham gia
        [HttpPost("apply")]
        public async Task<IActionResult> ApplyForMatch([FromBody] ApplyMatchDTO dto)
        {
            try
            {
                // 1. Kiểm tra chống Spam (Người dùng đã gửi đơn vào trận này chưa?)
                var existingApply = await _context.Matchinteractions
                    .FirstOrDefaultAsync(i => i.MatchRequestId == dto.MatchRequestId && i.UserId == dto.UserId);

                if (existingApply != null)
                {
                    return BadRequest(new { success = false, message = "Bạn đã gửi đơn xin vào trận này rồi!" });
                }
                // chặn xin tham gia vào yêu cầu tìm kiếm đồng đội của mình tạo
                var targetMatch = await _context.Matchrequests.FindAsync(dto.MatchRequestId);
                if (targetMatch != null && targetMatch.HostId == dto.UserId)
                {
                    return BadRequest(new { success = false, message = "Bạn không thể tự xin tham gia vào yêu cầu do chính mình tạo!" });
                }

                // 2. Tạo bản ghi Interaction mới
                var newInteraction = new Matchinteraction
                {
                    MatchRequestId = dto.MatchRequestId,
                    UserId = dto.UserId,
                    InteractionType = "Apply",
                    Message = dto.Message,
                    Status = "Pending",
                    CreatedAt = DateTime.UtcNow
                };

                _context.Matchinteractions.Add(newInteraction);
                await _context.SaveChangesAsync();


                // --- BẮN THÔNG BÁO ---
                var host = await _context.Users.FindAsync(targetMatch.HostId);
                if (!string.IsNullOrEmpty(host?.FcmToken))
                {
                    // Dùng bí danh FcmMessage
                    var message = new FcmMessage()
                    {
                        Token = host.FcmToken,
                        Notification = new FcmNotification() // Dùng bí danh FcmNotification
                        {
                            Title = "Có người xin tham gia!",
                            Body = $"Bạn có một yêu cầu mới từ {await _context.Users.Where(u => u.Id == dto.UserId).Select(u => u.FullName).FirstOrDefaultAsync()}"
                        }
                    };
                    await FirebaseMessaging.DefaultInstance.SendAsync(message);
                }

                return Ok(new { success = true, message = "Đã gửi yêu cầu xin vào đội thành công! Chờ chủ sân duyệt nhé." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = "Lỗi hệ thống server: " + ex.Message });
            }
        }

        // duyệt yêu cầu
        [HttpPatch("respond")]
        public async Task<IActionResult> RespondToApply([FromBody] RespondRequestDTO dto)
        {
            try
            {
                // 1. Tìm đơn xin tham gia trong DB (Kèm theo thông tin trận đấu gốc)
                var interaction = await _context.Matchinteractions
                    .Include(i => i.MatchRequest) // Load bảng MatchRequests để lấy thông tin số người
                    .FirstOrDefaultAsync(i => i.Id == dto.InteractionId);

                if (interaction == null)
                {
                    return NotFound(new { message = "Không tìm thấy yêu cầu này!" });
                }

                // Chặn việc spam duyệt 1 đơn nhiều lần
                if (interaction.Status != "Pending")
                {
                    return BadRequest(new { message = "Yêu cầu này đã được xử lý trước đó rồi!" });
                }

                // 2. Xử lý logic theo lựa chọn của Chủ sân
                if (dto.IsAccepted)
                {
                    // Đổi trạng thái đơn thành Chấp nhận
                    interaction.Status = "Accepted";

                    // Trừ số lượng người thiếu ở trận đấu đi 1
                    if (interaction.MatchRequest != null && interaction.MatchRequest.MissingPlayers > 0)
                    {
                        interaction.MatchRequest.MissingPlayers -= 1;

                        // Nếu trừ xong mà đủ người (MissingPlayers = 0) thì tự động đóng trận đấu
                        if (interaction.MatchRequest.MissingPlayers == 0)
                        {
                            interaction.MatchRequest.Status = "Full"; // Trận đấu đã đầy
                        }
                    }
                }
                else
                {
                    // Chủ sân chọn Từ chối
                    interaction.Status = "Rejected";
                }

                // 3. Lưu tất cả thay đổi xuống DB cùng một lúc
                await _context.SaveChangesAsync();

                // --- BẮN THÔNG BÁO CHO NGƯỜI XIN ---
                var sender = await _context.Users.FindAsync(interaction.UserId);
                if (!string.IsNullOrEmpty(sender?.FcmToken))
                {
                    // Dùng bí danh FcmMessage
                    var message = new FcmMessage()
                    {
                        Token = sender.FcmToken,
                        Notification = new FcmNotification() // Dùng bí danh FcmNotification
                        {
                            Title = "Kết quả duyệt kèo",
                            Body = dto.IsAccepted ? "Yêu cầu vào đội đã được CHẤP NHẬN!" : "Rất tiếc, yêu cầu vào đội đã bị TỪ CHỐI."
                        }
                    };
                    await FirebaseMessaging.DefaultInstance.SendAsync(message);
                }

                string resultMsg = dto.IsAccepted ? "Đã CHẤP NHẬN cho người này vào đội!" : "Đã TỪ CHỐI yêu cầu.";
                return Ok(new { success = true, message = resultMsg });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi hệ thống server: " + ex.Message });
            }
        }

        //  API LẤY DANH SÁCH THÔNG BÁO CHỜ DUYỆT
        [HttpGet("notifications/{hostId}")]
        public async Task<IActionResult> GetNotifications(int hostId)
        {
            try
            {
                // Tìm các đơn tương tác đang 'Pending' thuộc về những trận đấu do hostId này tạo ra
                var pendingRequests = await _context.Matchinteractions
                    .Include(i => i.MatchRequest)
                    .Include(i => i.User) // Để lấy thông tin người gửi đơn
                    .Where(i => i.MatchRequest.HostId == hostId && i.Status == "Pending")
                    .OrderByDescending(i => i.CreatedAt) // Đơn mới nhất lên đầu
                    .Select(i => new
                    {
                        Id = i.Id,
                        MatchRequestId = i.MatchRequestId,
                        UserId = i.UserId,
                        SenderName = i.User.FullName,         // Ánh xạ vào senderName bên Android
                        SportType = i.MatchRequest.SportType,   // Ánh xạ vào sportType bên Android
                        Message = i.Message,
                        Status = i.Status,
                        CreatedAt = i.CreatedAt.Value.ToString("yyyy-MM-ddTHH:mm:ss") // Chuẩn ISO cho Android dễ parse
                    })
                    .ToListAsync();

                return Ok(pendingRequests);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi hệ thống server: " + ex.Message });
            }
        }

        // API LẤY DANH SÁCH ĐƠN MÌNH ĐÃ GỬI XIN THAM GIA
        [HttpGet("my-requests/{userId}")]
        public async Task<IActionResult> GetMyRequests(int userId)
        {
            try
            {
                var myRequests = await _context.Matchinteractions
                    .Include(i => i.MatchRequest)
                    .ThenInclude(m => m.Host) // Kéo theo bảng User để lấy tên Chủ sân
                    .Where(i => i.UserId == userId && i.InteractionType == "Apply")
                    .OrderByDescending(i => i.CreatedAt)
                    .Select(i => new
                    {
                        Id = i.Id,
                        MatchRequestId = i.MatchRequestId,
                        HostName = i.MatchRequest.Host.FullName, // Tên chủ sân để hiển thị
                        SportType = i.MatchRequest.SportType,
                        Message = i.Message,
                        Status = i.Status, // Pending, Accepted, hoặc Rejected
                        CreatedAt = i.CreatedAt.Value.ToString("yyyy-MM-ddTHH:mm:ss")
                    })
                    .ToListAsync();

                return Ok(myRequests);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi hệ thống server: " + ex.Message });
            }
        }

        [HttpDelete("cancel-request/{interactionId}")]
        public async Task<IActionResult> CancelRequest(int interactionId)
        {
            try
            {
                var interaction = await _context.Matchinteractions.FindAsync(interactionId);
                if (interaction == null) return NotFound(new { message = "Không tìm thấy yêu cầu này!" });

                // Chỉ cho phép hủy nếu chủ sân chưa duyệt
                if (interaction.Status != "Pending")
                {
                    return BadRequest(new { message = "Không thể hủy vì chủ sân đã xử lý yêu cầu này rồi!" });
                }

                _context.Matchinteractions.Remove(interaction);
                await _context.SaveChangesAsync();

                return Ok(new { success = true, message = "Đã hủy yêu cầu tham gia thành công." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi hệ thống server: " + ex.Message });
            }
        }

    }
}