using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SportMatchAPI.DTO;
using SportMatchAPI.Data;

namespace SportMatchAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class UserController : ControllerBase
    {
        private readonly AppDbContext _context;

        public UserController(AppDbContext context)
        {
            _context = context;
        }

        [HttpPost("update-profile")]
        public async Task<IActionResult> UpdateProfile([FromBody] UpdateProfileDTO request)
        {
            // 1. Kiểm tra dữ liệu đầu vào
            if (request == null || string.IsNullOrWhiteSpace(request.FullName))
            {
                return BadRequest(new { message = "Họ tên không được để trống!" });
            }

            // 2. Tìm người dùng trong Database MySQL theo ID
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Id == request.Id);

            if (user == null)
            {
                return NotFound(new { message = "Không tìm thấy tài khoản trong hệ thống." });
            }

            // 3. Cập nhật thông tin mới
            user.FullName = request.FullName.Trim();
            user.AvatarUrl = request.AvatarUrl; // Cho phép null nếu họ xóa ảnh


            // 4. Lưu xuống Database
            await _context.SaveChangesAsync();

            // 5. Trả về Response khớp 100% với cấu trúc AuthResponseDto mà Retrofit Android đang chờ
            var responseData = new
            {
                message = "Cập nhật thành công!",
                token = "", // Trả về chuỗi rỗng vì update profile không cần đổi token
                user = new
                {
                    id = user.Id,
                    fullName = user.FullName,
                    phoneNumber = user.PhoneNumber,
                    avatarUrl = user.AvatarUrl,
                    createdAt = user.CreatedAt.HasValue ? user.CreatedAt.Value.ToString("dd/MM/yyyy") : "Chưa xác định"
                }
            };

            return Ok(responseData);
        }

        [HttpPost("logout")]
        public async Task<IActionResult> Logout([FromBody] LogoutRequestDTO request)
        {
            if (request == null || request.UserId <= 0)
            {
                return BadRequest(new { message = "Dữ liệu yêu cầu không hợp lệ." });
            }

            // 1. Tìm người dùng trong Database
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Id == request.UserId);

            if (user == null)
            {
                return NotFound(new { message = "Không tìm thấy người dùng." });
            }

            try
            {
                // 2. XỬ LÝ BẢO MẬT: Xóa Token FCM Push Notification của thiết bị này trên Server
                // Giả sử Hào đang lưu FcmToken trong bảng Users hoặc một bảng cấu hình Token riêng.
                // Cần gán nó về null hoặc xóa bản ghi thiết bị đó đi để chặn nhận thông báo "ma" sau khi thoát.

                user.FcmToken = null; // Bật dòng này nếu Hào lưu trực tiếp cột FcmToken trong bảng Users

                await _context.SaveChangesAsync();

                return Ok(new { message = "Đăng xuất phía Server thành công, đã hủy liên kết thiết bị!" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Có lỗi xảy ra khi hủy phiên đăng xuất.", error = ex.Message });
            }
        }

        [HttpPost("upload-avatar")]
        public async Task<IActionResult> UploadAvatar([FromForm] IFormFile file, [FromForm] int userId)
        {
            // 1. Kiểm tra file hợp lệ
            if (file == null || file.Length == 0)
            {
                return BadRequest(new { message = "Không tìm thấy file ảnh tải lên!" });
            }

            // 2. Tìm User trong DB
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Id == userId);
            if (user == null)
            {
                return NotFound(new { message = "Người dùng không tồn tại." });
            }

            try
            {
                // 3. Khởi tạo đường dẫn đến thư mục wwwroot/avatars
                var uploadsFolder = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "avatars");

                // Tự động tạo thư mục trên ổ cứng nếu chưa có
                if (!Directory.Exists(uploadsFolder))
                {
                    Directory.CreateDirectory(uploadsFolder);
                }

                // 4. Tạo tên file duy nhất bằng Guid để không bao giờ bị ghi đè trùng tên
                var extension = Path.GetExtension(file.FileName);
                var uniqueFileName = $"avatar_user_{userId}_{Guid.NewGuid()}{extension}";
                var filePath = Path.Combine(uploadsFolder, uniqueFileName);

                // 5. Coppy dữ liệu file vào ổ cứng Server
                using (var fileStream = new FileStream(filePath, FileMode.Create))
                {
                    await file.CopyToAsync(fileStream);
                }

                // 6. Tự động sinh đường link URL động dựa theo Host đang chạy (Hỗ trợ cả Localhost lẫn IP máy thật)
                var requestScheme = Request.Scheme; // http hoặc https
                var requestHost = Request.Host;     // VD: 10.0.2.2:5020 hoặc 192.168.1.5:5020
                var publicAvatarUrl = $"{requestScheme}://{requestHost}/avatars/{uniqueFileName}";

                // 7. Lưu link URL mới này vào MySQL
                user.AvatarUrl = publicAvatarUrl;
                await _context.SaveChangesAsync();

                // 8. Trả về cho Android cập nhật giao diện
                return Ok(new
                {
                    message = "Tải ảnh lên Server thành công!",
                    avatarUrl = publicAvatarUrl
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi xử lý file trên Server", error = ex.Message });
            }
        }

    }

}