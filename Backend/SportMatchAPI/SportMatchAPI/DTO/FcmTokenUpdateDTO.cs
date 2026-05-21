namespace SportMatchAPI.DTO
{
    public class FcmTokenUpdateDTO
    {
        public int UserId { get; set; }
        public string Token { get; set; } = string.Empty;
    }
}