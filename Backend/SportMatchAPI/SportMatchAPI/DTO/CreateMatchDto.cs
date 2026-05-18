namespace SportMatchAPI.DTO
{
    public class CreateMatchDto
    {
        public int HostId { get; set; }
        public string SportType { get; set; } = string.Empty;
        public string RequestType { get; set; } = string.Empty;
        public int MissingPlayers { get; set; }
        public string? Description { get; set; }
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
    }
}
