using FirebaseAdmin.Auth;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using SportMatchAPI.Data;
using SportMatchAPI.DTO;
using SportMatchAPI.Models;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace SportMatchAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IConfiguration _configuration;

        // Tiêm DbContext và Configuration vào
        public AuthController(AppDbContext context, IConfiguration configuration)
        {
            _context = context;
            _configuration = configuration;
        }

        [HttpPost("verify-phone")]
        public async Task<IActionResult> VerifyPhoneAuth([FromBody] VerifyFirebaseRequest request)
        {
            Console.WriteLine($"==> NHẬN ĐƯỢC REQUEST VỚI TOKEN: {request.IdToken.Substring(0, 10)}...");
            try
            {
                // 1. Xác thực Token với server của Google/Firebase
                FirebaseToken decodedToken = await FirebaseAuth.DefaultInstance.VerifyIdTokenAsync(request.IdToken);
                string firebaseUid = decodedToken.Uid;

                // 2. Lấy số điện thoại từ Token Claims
                if (!decodedToken.Claims.TryGetValue("phone_number", out object? phoneObj) || phoneObj == null)
                {
                    return BadRequest("Token không chứa thông tin số điện thoại.");
                }
                string phoneNumber = phoneObj.ToString()!;

                // 3. Xử lý Logic Database (Đăng nhập / Đăng ký)
                var user = await _context.Users.FirstOrDefaultAsync(u => u.FirebaseUid == firebaseUid || u.PhoneNumber == phoneNumber);

                if (user == null)
                {
                    // Lấy 4 số cuối của số điện thoại để tạo tên gợi nhớ
                    // Ví dụ: +84999999999 -> User9999
                    string lastFourDigits = phoneNumber.Length >= 4
                        ? phoneNumber.Substring(phoneNumber.Length - 4)
                        : new Random().Next(1000, 9999).ToString();

                    // TẠO TÀI KHOẢN MỚI
                    user = new User
                    {
                        FirebaseUid = firebaseUid,
                        PhoneNumber = phoneNumber,
                        // Thay đổi dòng này theo ý bạn
                        FullName = $"User_{lastFourDigits}"
                    };

                    _context.Users.Add(user);
                    await _context.SaveChangesAsync();
                    Console.WriteLine($"==> ĐÃ TẠO USER MỚI: {user.FullName}");
                }
                else if (string.IsNullOrEmpty(user.FirebaseUid))
                {
                    // Cập nhật FirebaseUid nếu user đã có trong DB từ trước nhưng chưa link Firebase
                    user.FirebaseUid = firebaseUid;
                    await _context.SaveChangesAsync();
                }

                // 4. Tạo JWT Token của hệ thống SportMatch
                string systemToken = GenerateJwtToken(user);

                // 5. Trả về Token kèm thông tin User cho App Android
                return Ok(new
                {
                    Message = "Xác thực thành công",
                    Token = systemToken,
                    User = new
                    {
                        user.Id,
                        user.PhoneNumber,
                        user.FullName,
                        user.AvatarUrl
                    }
                });
            }
            catch (FirebaseAuthException ex)
            {
                // Lỗi khi Token gửi lên từ App là đồ giả hoặc đã hết hạn
                //return Unauthorized("Firebase Token không hợp lệ hoặc đã hết hạn.");
                // DÒNG NÀY SẼ HIỆN LỖI CHI TIẾT TRÊN CONSOLE
                Console.WriteLine("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Console.WriteLine($"[LỖI FIREBASE]: {ex.Message}");
                Console.WriteLine($"[MÃ LỖI]: {ex.AuthErrorCode}");
                Console.WriteLine("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                return Unauthorized(ex.Message);
            }
            catch (Exception ex)
            {
                return StatusCode(500, $"Lỗi server: {ex.Message}");
            }
        }

        // ==========================================
        // HÀM PHỤ TRỢ: TẠO MÃ JWT TOKEN
        // ==========================================
        private string GenerateJwtToken(User user)
        {
            // Đọc Key từ Secret Manager (Không bao giờ gán cứng ở đây)
            var secretKey = _configuration["Jwt:Key"];

            // Báo lỗi ngay lập tức nếu quên chưa nạp Key vào Server
            if (string.IsNullOrEmpty(secretKey))
            {
                throw new Exception("LỖI CẤU HÌNH: Chưa thiết lập khóa Jwt:Key trên Server/Secret Manager.");
            }

            var key = Encoding.UTF8.GetBytes(secretKey);

            // Đóng gói thông tin cơ bản của người dùng vào Token (Không đóng gói Password hay Dữ liệu nhạy cảm)
            var claims = new List<Claim>
            {
                new Claim(JwtRegisteredClaimNames.Sub, user.Id.ToString()),
                new Claim(ClaimTypes.MobilePhone, user.PhoneNumber),
                new Claim(ClaimTypes.Name, user.FullName ?? "")
            };

            // Cấu hình thời hạn của Token (Ví dụ: Cấp thẻ có thời hạn 30 ngày)
            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(claims),
                Expires = DateTime.UtcNow.AddDays(30),
                SigningCredentials = new SigningCredentials(new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature)
            };

            var tokenHandler = new JwtSecurityTokenHandler();
            var token = tokenHandler.CreateToken(tokenDescriptor);

            return tokenHandler.WriteToken(token);
        }

        [HttpPost("update-token")]
        public async Task<IActionResult> UpdateFcmToken([FromBody] FcmTokenUpdateDTO dto)
        {
            try
            {
                // dto chứa UserId và Token từ Android gửi lên
                var user = await _context.Users.FindAsync(dto.UserId);
                if (user == null) return NotFound("Không tìm thấy người dùng.");

                user.FcmToken = dto.Token; // Lưu token vào cột mới tạo
                await _context.SaveChangesAsync();

                return Ok(new { success = true, message = "Đã lưu Token thành công!" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, $"Lỗi server: {ex.Message}");
            }
        }
    }
}