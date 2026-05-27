using System.Text.Json.Serialization;

namespace SportMatchAPI.DTO
{
    public class VerifyFirebaseDTO
    {
        [JsonPropertyName("idToken")] // Đảm bảo khớp chính xác với key "idToken" từ Android gửi sang
        public string IdToken { get; set; } = null!;
    }
}