using System;
using System.Collections.Generic;

namespace SportMatchAPI.Models;

public partial class Matchinteraction
{
    public int Id { get; set; }

    public int MatchRequestId { get; set; }

    public int UserId { get; set; }

    public string? InteractionType { get; set; }

    public string? Message { get; set; }

    public string? Status { get; set; }

    public DateTime? CreatedAt { get; set; }

    public virtual Matchrequest MatchRequest { get; set; } = null!;

    public virtual User User { get; set; } = null!;
}
