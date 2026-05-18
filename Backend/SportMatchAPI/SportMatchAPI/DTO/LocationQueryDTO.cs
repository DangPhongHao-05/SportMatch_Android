namespace SportMatchAPI.DTO
{
    public class LocationQueryDTO
    {
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public double RadiusInKm { get; set; } = 5; // Mặc định quét bán kính 5km
    }
}
