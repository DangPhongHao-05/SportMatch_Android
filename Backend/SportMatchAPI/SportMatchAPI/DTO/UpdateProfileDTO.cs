namespace SportMatchAPI.DTO
{
    public class UpdateProfileDTO
    {
        public int Id { get; set; }
        public string FullName { get; set; } = null!;
        public string? AvatarUrl { get; set; }
    }
}
