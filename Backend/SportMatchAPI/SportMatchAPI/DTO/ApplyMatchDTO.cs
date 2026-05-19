namespace SportMatchAPI.DTO
{
    public class ApplyMatchDTO
    {
        public int MatchRequestId { get; set; }
        public int UserId { get; set; }
        public string Message { get; set; } = string.Empty;
    }
}
