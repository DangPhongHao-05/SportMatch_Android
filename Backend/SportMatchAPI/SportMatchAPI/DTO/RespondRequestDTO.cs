namespace SportMatchAPI.DTO
{
    public class RespondRequestDTO
    {
        public int InteractionId { get; set; } // ID của cái đơn xin tham gia
        public bool IsAccepted { get; set; }  // True = Đồng ý, False = Từ chối
    }
}
